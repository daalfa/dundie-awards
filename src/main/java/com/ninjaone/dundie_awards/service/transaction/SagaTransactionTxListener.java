package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.AwardsCache;
import com.ninjaone.dundie_awards.model.event.SagaTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class SagaTransactionTxListener {

    private final AwardsCache awardsCache;

    public SagaTransactionTxListener(final AwardsCache awardsCache) {
        this.awardsCache = awardsCache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void onSuccess(SagaTransactionEvent event) {
        log.info("SagaTransactionTxListener.onSuccess transaction commited for Activity {}", event);
        awardsCache.decrementAwards(event.totalAwardsToDecrement());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void onError(SagaTransactionEvent event) {
        log.error("SagaTransactionTxListener.onError transaction rollback for Activity {}", event);
    }
}
