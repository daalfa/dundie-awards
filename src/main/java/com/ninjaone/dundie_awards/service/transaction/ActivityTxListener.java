package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.model.event.ActivityEvent;
import com.ninjaone.dundie_awards.service.AwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ActivityTxListener {

    private final AwardService awardService;

    public ActivityTxListener(final AwardService awardService) {
        this.awardService = awardService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void onSuccess(ActivityEvent event) {
        log.info("ActivityTxListener.onSuccess transaction commited for Activity {}", event);
        awardService.confirmAwardTransaction(event.transactionSagaId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void onError(ActivityEvent event) {
        log.error("ActivityTxListener.onError transaction rollback for Activity {}", event);
        awardService.processAwardRollback(event.transactionSagaId());
    }
}
