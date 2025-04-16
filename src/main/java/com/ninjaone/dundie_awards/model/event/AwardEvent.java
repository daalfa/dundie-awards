package com.ninjaone.dundie_awards.model.event;

public record AwardEvent(
        int awardsGiven,
        Long organizationId,
        String transactionSagaId) {
}
