package com.zhihui.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisUtilsTest {
    @Autowired
    private RedisUtils redisUtils;

    @Test
    void testSetAndGet() {
        redisUtils.set("test:user", "张三");
        assertEquals("张三", redisUtils.get("test:user"));
    }

    @Test
    void testExpire() throws InterruptedException {
        redisUtils.set("test:expire", "tmp", 2);
        assertTrue(redisUtils.hasKey("test:expire"));
        Thread.sleep(2500);
        assertFalse(redisUtils.hasKey("test:expire"));
    }

    @Test
    void testDelete() {
        redisUtils.set("test:del", "value");
        assertTrue(redisUtils.delete("test:del"));
        assertFalse(redisUtils.hasKey("test:del"));
    }

    @Test
    void testIncr() {
        redisUtils.delete("test:counter");
        assertEquals(1, redisUtils.incr("test:counter", 1));
        assertEquals(6, redisUtils.incr("test:counter", 5));
    }

    @Test
    void testHash() {
        redisUtils.hSet("test:hash", "name", "李四");
        redisUtils.hSet("test:hash", "age", 25);
        assertEquals("李四", redisUtils.hGet("test:hash", "name"));
        assertEquals(25, (Integer) redisUtils.hGet("test:hash", "age"));
    }
}