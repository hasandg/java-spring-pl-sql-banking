package com.banking.listener;

import com.banking.event.TransactionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class CacheInvalidationListener {

    private final CacheManager cacheManager;

    public CacheInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        try {
            log.info("Invalidating cache for account: {} after {} transaction", 
                    event.getAccountNumber(), event.getTransactionType());
            
            var accountsCache = cacheManager.getCache("accounts");
            if (accountsCache != null) {
                accountsCache.evict(event.getAccountNumber());
            }
            
            var transactionsCache = cacheManager.getCache("transactions");
            if (transactionsCache != null) {
                transactionsCache.evict(event.getAccountNumber());
            }
            
            log.debug("Cache invalidated successfully for account: {}", event.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to invalidate cache for account: {}", event.getAccountNumber(), e);
        }
    }
} 