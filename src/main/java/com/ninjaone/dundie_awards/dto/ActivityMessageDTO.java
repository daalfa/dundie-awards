package com.ninjaone.dundie_awards.dto;

import java.time.LocalDateTime;

public record ActivityMessageDTO(
        LocalDateTime occuredAt,
        String event,
        String transactionSagaId) {
}
