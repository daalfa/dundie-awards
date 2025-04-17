package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.SagaTransactionResponseDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ninjaone.dundie_awards.MessageBroker.SAGA_QUEUE;

@Slf4j
@Service
public class SagaTransactionService {

    private final AwardService awardService;

    public SagaTransactionService(AwardService awardService) {
        this.awardService = awardService;
    }

    @SqsListener(queueNames = SAGA_QUEUE, acknowledgementMode = "ALWAYS")
    protected void listener(SagaTransactionResponseDTO message) {
        log.info("Saga transaction response received! {}", message);

        if(message.isSagaCompleted()) {
            awardService.confirmAwardTransaction(message.transactionSagaId());
        } else {
            awardService.processAwardRollback(message.transactionSagaId());
        }
    }
}
