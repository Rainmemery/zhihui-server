package com.zhihui.common.lock;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式锁，支持看门狗（Watchdog）自动续期机制。
 *
 * <h3>看门狗机制说明：</h3>
 * <p>当业务执行时间超过锁的 TTL 时，锁会自动过期导致并发问题。
 * 看门狗通过后台定时任务，在锁过期前自动续期，确保业务执行期间锁不会丢失。</p>
 *
 * <h3>续期策略：</h3>
 * <ul>
 *   <li>续期间隔 = TTL / 3（与 Redisson 默认策略一致）</li>
 *   <li>续期使用 Lua 脚本保证原子性，仅当锁的 value 与持有者一致时才续期</li>
 *   <li>续期失败（锁已不存在或被他人持有）时自动停止看门狗</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 *   String uuid = UUID.randomUUID().toString();
 *   if (redisLock.tryLockWithWatchdog("order:123", uuid, 30)) {
 *       try {
 *           // 执行业务逻辑，即使超过 30 秒也不会丢锁
 *       } finally {
 *           redisLock.unlock("order:123", uuid);
 *       }
 *   }
 * </pre>
 */
@Component
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /** 锁 key 的统一前缀，避免与其他 Redis key 冲突 */
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 看门狗定时任务线程池
     * <p>核心线程数 = CPU 核心数，使用守护线程避免阻塞 JVM 退出。</p>
     * <p>每个加锁成功的 key 会提交一个定时续期任务到该线程池。</p>
     */
    private final ScheduledExecutorService watchdogExecutor =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread t = new Thread(r, "redis-lock-watchdog");
                        t.setDaemon(true);
                        return t;
                    });

    /**
     * 锁 key -> 看门狗续期任务的映射
     * <p>用于在解锁时取消对应的定时续期任务，防止无意义的续期操作。</p>
     * <p>使用 ConcurrentHashMap 保证多线程下的并发安全。</p>
     */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> watchdogTasks = new ConcurrentHashMap<>();

    /**
     * 解锁 Lua 脚本
     * <p>先校验锁的 value 是否与当前持有者一致，一致才删除，避免误删他人的锁。</p>
     * <p>场景：A 持有锁过期后，B 获取了锁，此时 A 执行 unlock 如果不校验 value 就会误删 B 的锁。</p>
     *
     * <ul>
     *   <li>KEYS[1] = 锁的完整 key（LOCK_PREFIX + key）</li>
     *   <li>ARGV[1] = 锁的 value（持有者标识）</li>
     * </ul>
     *
     * @return 1 表示解锁成功，0 表示锁不属于当前持有者
     */
    private static final String UNLOCK_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            else
                return 0
            end
            """;

    /**
     * 续期 Lua 脚本
     * <p>先校验锁的 value 是否与当前持有者一致，一致才续期，避免误续他人的锁。</p>
     * <p>场景：A 持有锁过期后，B 获取了锁，此时 A 的看门狗如果继续续期就会覆盖 B 的锁。</p>
     *
     * <ul>
     *   <li>KEYS[1] = 锁的完整 key（LOCK_PREFIX + key）</li>
     *   <li>ARGV[1] = 锁的 value（持有者标识）</li>
 *    *   <li>ARGV[2] = 续期的 TTL（秒）</li>
     * </ul>
     *
     * @return 1 表示续期成功，0 表示锁不属于当前持有者（看门狗应停止）
     */
    private static final String RENEW_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('EXPIRE', KEYS[1], ARGV[2])
            else
                return 0
            end
            """;

    /**
     * 加锁（非阻塞，无看门狗自动续期）
     * <p>使用 Redis 的 SETNX + EX 原子命令加锁，锁到期后自动释放。</p>
     * <p>适用于业务执行时间可控、远小于 TTL 的场景。</p>
     *
     * @param key           锁的业务标识（如 "order:123"），会自动添加 LOCK_PREFIX 前缀
     * @param value         锁的持有者标识（建议使用 UUID），用于解锁时校验身份，防止误删他人锁
     * @param expireSeconds 锁的过期时间（秒），到期后 Redis 自动删除，防止死锁
     * @return true 加锁成功，false 表示锁已被他人持有
     */
    public boolean tryLock(String key, String value, long expireSeconds) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(LOCK_PREFIX + key, value, Duration.ofSeconds(expireSeconds))
        );
    }

    /**
     * 加锁（非阻塞，带看门狗自动续期）
     * <p>加锁成功后启动看门狗，看门狗每隔 TTL/3 秒自动续期一次，
     * 确保业务未执行完时锁不会过期。调用 {@link #unlock} 时自动停止看门狗。</p>
     *
     * <h4>续期流程：</h4>
     * <ol>
     *   <li>加锁成功后，启动定时任务，每隔 TTL/3 秒执行一次续期</li>
     *   <li>续期时通过 Lua 脚本校验 value，仅续期属于自己的锁</li>
     *   <li>续期失败（锁已不存在或被他人持有）时自动停止看门狗</li>
     *   <li>业务执行完毕调用 unlock，停止看门狗并释放锁</li>
     * </ol>
     *
     * @param key           锁的业务标识（如 "order:123"），会自动添加 LOCK_PREFIX 前缀
     * @param value         锁的持有者标识（建议使用 UUID），用于续期和解锁时校验身份
     * @param expireSeconds 锁的 TTL（秒），看门狗每次续期都会重置为此值
     * @return true 加锁成功，false 表示锁已被他人持有
     */
    public boolean tryLockWithWatchdog(String key, String value, long expireSeconds) {
        boolean locked = tryLock(key, value, expireSeconds);
        if (locked) {
            startWatchdog(key, value, expireSeconds);
        }
        return locked;
    }

    /**
     * 启动看门狗定时续期任务
     * <p>使用 {@link ScheduledExecutorService#scheduleAtFixedRate} 以固定频率执行续期，
     * 续期间隔为 TTL / 3（最小 1 秒），与 Redisson 的默认策略一致。</p>
     *
     * <h4>关键设计：</h4>
     * <ul>
     *   <li>续期间隔 = TTL/3：确保在锁过期前有充足的时间续期，即使某次续期延迟也不会导致锁过期</li>
     *   <li>Lua 脚本校验 value：防止看门狗误续他人的锁（如锁已过期被他人获取）</li>
     *   <li>续期失败自动停止：锁已不存在或 value 不匹配时，说明锁已被释放或被他人持有，停止续期</li>
     *   <li>异常时停止：Redis 连接异常等情况下停止续期，避免无限重试</li>
     *   <li>替换旧任务：如果同一 key 已有看门狗在运行，先取消旧任务再注册新任务</li>
     * </ul>
     *
     * @param key           锁的业务标识
     * @param value         锁的持有者标识
     * @param expireSeconds 锁的 TTL（秒），每次续期重置为此值
     */
    private void startWatchdog(String key, String value, long expireSeconds) {
        long renewInterval = Math.max(expireSeconds / 3, 1);
        String lockKey = LOCK_PREFIX + key;
        ScheduledFuture<?> future = watchdogExecutor.scheduleAtFixedRate(() -> {
            try {
                Boolean result = redisTemplate.execute(
                        new DefaultRedisScript<>(RENEW_SCRIPT, Long.class),
                        List.of(lockKey), value, String.valueOf(expireSeconds)
                ).equals(1L);
                if (!Boolean.TRUE.equals(result)) {
                    stopWatchdog(key);
                }
            } catch (Exception e) {
                stopWatchdog(key);
            }
        }, renewInterval, renewInterval, TimeUnit.SECONDS);
        ScheduledFuture<?> old = watchdogTasks.put(key, future);
        if (old != null) {
            old.cancel(false);
        }
    }

    /**
     * 停止指定 key 的看门狗续期任务
     * <p>从映射中移除并取消定时任务。cancel(false) 表示不中断正在执行的续期操作。</p>
     *
     * @param key 锁的业务标识
     */
    private void stopWatchdog(String key) {
        ScheduledFuture<?> future = watchdogTasks.remove(key);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 解锁（Lua 脚本保证原子性，同时停止看门狗）
     * <p>先停止看门狗防止续期，再通过 Lua 脚本原子性地校验 value 并删除锁。</p>
     *
     * <h4>为什么必须先停止看门狗：</h4>
     * <p>如果先解锁再停止看门狗，在解锁后、停止前的短暂窗口内，
     * 看门狗可能恰好触发续期，导致锁被重新设置 TTL，造成死锁。</p>
     *
     * @param key   锁的业务标识
     * @param value 锁的持有者标识，必须与加锁时的 value 一致
     * @return true 解锁成功，false 表示锁不属于当前持有者（可能已过期被他人获取）
     */
    public boolean unlock(String key, String value) {
        stopWatchdog(key);
        return 1L == redisTemplate.execute(
                new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
                List.of(LOCK_PREFIX + key), value
        );
    }

    /**
     * 应用关闭时清理资源
     * <p>取消所有看门狗续期任务，清空任务映射，关闭线程池。</p>
     * <p>由于看门狗线程是守护线程，JVM 退出时会自动终止，
     * 但显式关闭可以更优雅地释放资源，避免 Redis 连接泄漏。</p>
     */
    @PreDestroy
    public void destroy() {
        watchdogTasks.values().forEach(f -> f.cancel(false));
        watchdogTasks.clear();
        watchdogExecutor.shutdownNow();
    }
}