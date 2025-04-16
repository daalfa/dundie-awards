package com.ninjaone.dundie_awards.model.event;

public record SagaTransactionEvent(
        String transactionSagaId,
        int totalAwardsToDecrement) {
}
