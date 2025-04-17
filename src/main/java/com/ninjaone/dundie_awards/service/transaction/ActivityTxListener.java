package com.ninjaone.dundie_awards.service.transaction;

import com.ninjaone.dundie_awards.MessageBroker;
import com.ninjaone.dundie_awards.dto.SagaTransactionResponseDTO;
import com.ninjaone.dundie_awards.exception.MessageBrokerException;
import com.ninjaone.dundie_awards.model.event.ActivityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ActivityTxListener {

    private final MessageBroker messageBroker;

    public ActivityTxListener(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void onSuccess(ActivityEvent event) throws MessageBrokerException {
        log.info("ActivityTxListener.onSuccess transaction commited for Activity {}", event);

        SagaTransactionResponseDTO message = new SagaTransactionResponseDTO(
                event.transactionSagaId(),
                true
        );
        messageBroker.sendMessage(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void onError(ActivityEvent event) throws MessageBrokerException {
        log.error("ActivityTxListener.onError transaction rollback for Activity {}", event);

        SagaTransactionResponseDTO message = new SagaTransactionResponseDTO(
                event.transactionSagaId(),
                false);
        messageBroker.sendMessage(message);
    }
}
