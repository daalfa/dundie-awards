package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.SagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaTransactionRepository extends JpaRepository<SagaTransaction, Long> {
    SagaTransaction findBySagaId(String sagaId);
}
