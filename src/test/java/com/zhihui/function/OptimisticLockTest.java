package com.zhihui.function;

import com.zhihui.common.exception.BusinessException;
import com.zhihui.common.exception.ErrorCode;
import com.zhihui.mapper.MeetingMapper;
import com.zhihui.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OptimisticLockTest {
    //后续完善测试逻辑，后续补全错误码体系
//    @Autowired
//    private MeetingService meetingService;
//    @Autowired private MeetingMapper meetingMapper;
//
//    @Test
//    void testConcurrentStatusTransition() throws Exception {
//        // 先创建一个 DRAFT 会议
//        // ... 创建逻辑，假设 meetingId = testMeetingId
//
//        int threadCount = 5;
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch endLatch = new CountDownLatch(threadCount);
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger conflictCount = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            new Thread(() -> {
//                try {
//                    startLatch.await(); // 等待发令枪
//                    meetingService.schedule(testMeetingId); // 同时排期
//                    successCount.incrementAndGet();
//                } catch (BusinessException e) {
//                    if (ErrorCode.VERSION_CONFLICT.equals(e.getCode())) {
//                        conflictCount.incrementAndGet();
//                    }
//                } catch (Exception ignored) {
//                } finally {
//                    endLatch.countDown();
//                }
//            }).start();
//        }
//
//        startLatch.countDown(); // 同时释放
//        endLatch.await(10, TimeUnit.SECONDS);
//
//        // 验证：仅 1 个线程成功，其余全部版本冲突
//        assertEquals(1, successCount.get());
//        assertEquals(threadCount - 1, conflictCount.get());
//    }
}
