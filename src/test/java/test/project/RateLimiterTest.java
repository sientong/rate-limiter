package test.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import test.project.processor.RateLimitProcessor;

public class RateLimiterTest 
{
    @Test
    public void testAllowUnderLimit() {
        RateLimitProcessor limiter = new RateLimitProcessor();
        for (int i = 0; i < 3; i++) {
            assertTrue("Should allow under limit", limiter.allow("account1", "request1"));
        }
    }

    @Test
    public void testRejectOverLimit() {
        RateLimitProcessor limiter = new RateLimitProcessor();
        for (int i = 0; i < 10; i++) {
            assertTrue("Initial requests should be allowed", limiter.allow("account1", "request1"));
        }
        assertFalse("6th request should be rejected", limiter.allow("account1", "request1"));
    }

    @Test
    public void testAllowUnderLimitWithDifferentUrl() {
        RateLimitProcessor limiter = new RateLimitProcessor();
        for (int i = 0; i < 10; i++) {
            assertTrue("Should allow under limit", limiter.allow("account1", "request1"));
        }

        for (int i = 0; i < 10; i++) {
            assertTrue("Should allow under limit", limiter.allow("account1", "request2"));
        }
    }

    @Test
    public void testAllowAfterCooldown() throws InterruptedException {
        RateLimitProcessor limiter = new RateLimitProcessor(3, 1000);
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allow("account1", "request1"));
        }
        assertFalse(limiter.allow("account1", "request1"));
        Thread.sleep(1100);
        for (int i = 0; i < 3; i++) {
            assertTrue("Should allow after cooldown", limiter.allow("account1", "request1"));
        }
    }

    @Test
    public void testSlidingWindowPrecision() throws InterruptedException {
        RateLimitProcessor limiter = new RateLimitProcessor(5, 1000);
        for (int i = 0; i < 5; i++) {
            assertTrue("Should allow spaced requests", limiter.allow("account1", "request1"));
            Thread.sleep(100);
        }
        Thread.sleep(500);
        assertTrue("Oldest request expired; should allow new one", limiter.allow("account1", "request1"));
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        final RateLimitProcessor limiter = new RateLimitProcessor(5, 1000);
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(1);
        final ConcurrentLinkedQueue<Boolean> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < 10; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                        results.add(limiter.allow("account1", "request1"));
                    } catch (InterruptedException ignored) {
                    }
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        long allowed = 0;
        for (Boolean result : results) {
            if (Boolean.TRUE.equals(result)) {
                allowed++;
            }
        }

        long rejected = results.size() - allowed;

        assertEquals("Exactly 5 should be allowed", 5, allowed);
        assertEquals("Remaining should be rejected", 5, rejected);
    }
}
