package com.ninjaone.dundie_awards.service.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ninjaone.dundie_awards.cache.AwardsCache;
import com.ninjaone.dundie_awards.MessageBroker;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.AwardEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.time.LocalDateTime.now;

@Slf4j
@Component
public class AwardTxListener {

    private final AwardsCache awardsCache;

    private final MessageBroker messageBroker;

    public AwardTxListener(
            final AwardsCache awardsCache,
            final MessageBroker messageBroker) {
        this.awardsCache = awardsCache;
        this.messageBroker = messageBroker;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void handleTransactionCommit(AwardEvent event) {
        log.info("Award transaction commit event for Organization {}", event.organizationId());
        awardsCache.addAwards(event.awardsGiven());

        String message = String.format(
                "Total of %s given awards to organization id %s", event.awardsGiven(), event.organizationId());

        Activity activity = new Activity(now(), message);
        messageBroker.sendMessage(activity);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void handleTransactionRollback(AwardEvent event) {
        log.warn("Award transaction rollback event for Organization {}", event.organizationId());
    }
}
