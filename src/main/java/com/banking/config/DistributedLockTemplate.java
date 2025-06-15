package com.banking.config;

import com.banking.exception.BankingOperationException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class DistributedLockTemplate {

    private final RedissonClient redissonClient;

    public DistributedLockTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T execute(String lockKey, Duration lockTimeout, Supplier<T> operation) {
        RLock lock = redissonClient.getLock("banking:lock:" + lockKey);
        
        try {
            boolean acquired = lock.tryLock(5, lockTimeout.toSeconds(), TimeUnit.SECONDS);
            if (!acquired) {
                throw new BankingOperationException("Could not acquire lock for: " + lockKey);
            }
            
            return operation.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankingOperationException("Lock acquisition interrupted for: " + lockKey, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void execute(String lockKey, Duration lockTimeout, Runnable operation) {
        execute(lockKey, lockTimeout, () -> {
            operation.run();
            return null;
        });
    }
} 