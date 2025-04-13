package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.AwardsCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class AwardTxListener {

    private final AwardsCache awardsCache;

    public AwardTxListener(AwardsCache awardsCache) {
        this.awardsCache = awardsCache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAwardsGivenAfterCommit(AwardTxEvent event) {
        log.info("Award transaction commit event for Organization {}", event.organizationId());
        awardsCache.addAwards(event.awardsGiven());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAwardsGivenRollback(AwardTxEvent event) {
        log.warn("Award transaction rollback event for Organization {}", event.organizationId());
    }
}
