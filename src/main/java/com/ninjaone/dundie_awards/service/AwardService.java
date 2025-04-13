package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.exception.NotFoundException;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.transaction.AwardTxEvent;
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

    private final ApplicationEventPublisher eventPublisher;

    public AwardService(
            final OrganizationRepository organizationRepository,
            final EmployeeRepository employeeRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AwardResponseDTO processAwardRequestByOrganization(Long orgId) {
        int awardsGiven = giveDundieAwardByOrganizationId(orgId);

        if (awardsGiven > 0) {
            log.debug("Publishing AwardsGivenEvent for orgId: {} with {} awards", orgId, awardsGiven);
            eventPublisher.publishEvent(new AwardTxEvent(awardsGiven, orgId));
        }

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
            log.debug("Award given to employee {} totaling {} awards", employee.getId(), awards);
            awardsGiven.getAndIncrement();
        });

        //todo: save this to ledger/audit table as pending

        return awardsGiven.get();
    }
}
