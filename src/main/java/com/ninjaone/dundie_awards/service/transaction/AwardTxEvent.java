package com.ninjaone.dundie_awards.service.transaction;

public record AwardTxEvent(
        int awardsGiven,
        Long organizationId) {
}
