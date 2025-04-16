package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.exception.NotFoundException;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.SagaStatus;
import com.ninjaone.dundie_awards.model.SagaTransaction;
import com.ninjaone.dundie_awards.model.event.SagaTransactionEvent;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.model.event.AwardEvent;
import com.ninjaone.dundie_awards.repository.SagaTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class AwardService {

    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final SagaTransactionRepository sagaTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AwardService(
            final OrganizationRepository organizationRepository,
            final EmployeeRepository employeeRepository,
            final SagaTransactionRepository sagaTransactionRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.sagaTransactionRepository = sagaTransactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AwardResponseDTO processAwardRequestByOrganization(Long orgId) {
        int awardsGiven = giveDundieAwardByOrganizationId(orgId);
        return new AwardResponseDTO("Given awards: "+awardsGiven, true);
    }

    private int giveDundieAwardByOrganizationId(Long orgId) {
        log.info("giveDundieAwardByOrganizationId {}", orgId);

        AtomicInteger awardsGiven = new AtomicInteger(0);

        List<Employee> employeeList = organizationRepository.findById(orgId)
                .map(employeeRepository::findByOrganization)
                .orElseThrow(() -> {
                    log.error("Organization {} not found.", orgId);
                    return new NotFoundException("Organization not found by id: "+orgId);
                });

        employeeList.forEach(employee -> {
            int awards = ofNullable(employee.getDundieAwards()).orElse(0);
            awards++;
            employee.setDundieAwards(awards);
            employeeRepository.save(employee);
            awardsGiven.getAndIncrement();
            log.info("Employee {} has now {} awards", employee.getId(), awards);
        });

        SagaTransaction sagaTransaction = new SagaTransaction(
                SagaStatus.PENDING, employeeList.stream().map(Employee::getId).toList(),
                1);
        sagaTransactionRepository.save(sagaTransaction);
        log.info("sagaTransaction: {}", sagaTransaction.getSagaId());

        if (awardsGiven.get() > 0) {
            log.info("Publishing AwardsGivenEvent for orgId: {} with {} awards", orgId, awardsGiven);
            eventPublisher.publishEvent(new AwardEvent(awardsGiven.get(), orgId, sagaTransaction.getSagaId()));
        }

        return awardsGiven.get();
    }

    @Transactional
    public void processAwardRollback(String transactionSagaId) {
        SagaTransaction sagaTransaction = sagaTransactionRepository.findBySagaId(transactionSagaId);

        if(sagaTransaction == null) {
            log.error("AwardService.processAwardRollback SagaTransaction sagaId do not exist");
            throw new RuntimeException("AwardService.processAwardRollback SagaTransaction sagaId dont exist");
        }

        List<Long> employeeIds = sagaTransaction.getEmployeeIds();
        int awardsEach = ofNullable(sagaTransaction.getAwardsEach()).orElse(0);
        employeeRepository.decrementDundieAwards(employeeIds, awardsEach);

        sagaTransaction.setStatus(SagaStatus.FAILED);
        sagaTransactionRepository.save(sagaTransaction);

        int totalAwardsToDecrement = awardsEach * employeeIds.size();
        eventPublisher.publishEvent(new SagaTransactionEvent(transactionSagaId, totalAwardsToDecrement));
    }

    @Transactional
    public void confirmAwardTransaction(String transactionSagaId) {
        SagaTransaction sagaTransaction = sagaTransactionRepository.findBySagaId(transactionSagaId);

        if(sagaTransaction == null) {
            log.error("AwardService.confirmAwardTransaction SagaTransaction sagaId do not exist");
            throw new RuntimeException("AwardService.processAwardRollback SagaTransaction sagaId dont exist");
        }

        sagaTransaction.setStatus(SagaStatus.COMPLETED);
        sagaTransactionRepository.save(sagaTransaction);
    }
}
