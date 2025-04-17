package com.ninjaone.dundie_awards.model;

import com.ninjaone.dundie_awards.repository.ListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "saga_transactions")
public class SagaTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", unique = true, nullable = false, updatable = false)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    @Lob
    @Convert(converter = ListConverter.class)
    @Column(updatable = false)
    private List<Long> employeeIds;

//    @Column(updatable = false)
    private int awardsEach;

//    @Column(updatable = false)
    private int awardsGiven;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    private void initSagaId() {
        if (this.sagaId == null) {
            this.sagaId = UUID.randomUUID().toString();
        }
    }

    public SagaTransaction(
            SagaStatus status,
            List<Long> employeeIds,
            int awardsEach,
            int awardsGiven) {
        this.status = status;
        this.employeeIds = employeeIds;
        this.awardsEach = awardsEach;
        this.awardsGiven = awardsGiven;
    }
}
