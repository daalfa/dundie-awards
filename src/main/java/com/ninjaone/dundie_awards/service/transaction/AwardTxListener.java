package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.AwardsCache;
import com.ninjaone.dundie_awards.MessageBroker;
import com.ninjaone.dundie_awards.dto.ActivityMessageDTO;
import com.ninjaone.dundie_awards.exception.MessageBrokerException;
import com.ninjaone.dundie_awards.model.event.AwardEvent;
import com.ninjaone.dundie_awards.service.AwardService;
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

    private final AwardService awardService;

    public AwardTxListener(
            final AwardsCache awardsCache,
            final MessageBroker messageBroker,
            final AwardService awardService) {
        this.awardsCache = awardsCache;
        this.messageBroker = messageBroker;
        this.awardService = awardService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void onSuccess(AwardEvent event) {
        log.info("AwardTxListener.onSuccess transaction commited for organizationId: {}", event.organizationId());
        awardsCache.incrementAwards(event.awardsGiven());

        String message = String.format(
                "Total of %s given awards to organization id %s", event.awardsGiven(), event.organizationId());

        ActivityMessageDTO activityMessageDTO = new ActivityMessageDTO(now(), message, event.transactionSagaId());

        try {
            messageBroker.sendMessage(activityMessageDTO);
            log.info("AwardTxListener.onSuccess message successfully sent to Queue.");
        } catch (MessageBrokerException e) {
            log.error("AwardTxListener.onSuccess message broker returned error. Will rollback awards.");
            awardService.processAwardRollback(event.transactionSagaId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void onError(AwardEvent event) {
        log.error("AwardTxListener.onError transaction rollback for organizationId {}", event.organizationId());
    }
}
