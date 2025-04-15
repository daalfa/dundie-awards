package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.model.Activity;
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
    protected void onSuccess(Activity event) {
        log.info("ActivityTxListener.onSuccess transaction commited for Activity {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void onError(Activity event) {
        log.warn("ActivityTxListener.onError transaction rollback for Activity {}", event);
        awardService.processAwardRollback(event);
    }
}
