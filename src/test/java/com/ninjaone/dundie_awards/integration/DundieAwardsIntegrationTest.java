package com.ninjaone.dundie_awards.integration;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.model.SagaStatus;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.repository.SagaTransactionRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;


public class DundieAwardsIntegrationTest extends BaseIntegrationTest {

    private static final String PATH = "/give-dundie-award";

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @MockitoSpyBean
    ActivityRepository activityRepository;

    @Autowired
    SagaTransactionRepository sagaTransactionRepository;

    @Autowired
    TestRestTemplate restTemplate;

    @AfterEach
    void afterEach() {
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
        activityRepository.deleteAll();
        sagaTransactionRepository.deleteAll();
    }

    @Test
    void giveDundieAwardTest() {
        var organization = new Organization("test organization");
        organizationRepository.save(organization);

        long orgId = organization.getId();

        var employee = new Employee("first", "last", organization);
        employeeRepository.save(employee);

        var response = restTemplate.postForEntity(
                PATH + "/" +orgId,
                null,
                AwardResponseDTO.class);

        assertThat(response.getBody()).extracting(AwardResponseDTO::message).isEqualTo("Given awards: 1");
        assertThat(response.getBody()).extracting(AwardResponseDTO::success).isEqualTo(true);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var activities = activityRepository.findAll();
                    assertThat(activities).hasSize(1);

                    var activity = activities.get(0);
                    assertThat(activity.getEvent()).isEqualTo("Total of 1 given awards to organization id "+orgId);

                    var transactions = sagaTransactionRepository.findAll();
                    assertThat(transactions).hasSize(1);

                    var transaction = transactions.get(0);
                    assertThat(transaction.getAwardsGiven()).isOne();
                    assertThat(transaction.getAwardsEach()).isOne();
                    assertThat(transaction.getStatus()).isEqualTo(SagaStatus.COMPLETED);
                });
    }

    @Test
    void giveDundieAwardRollBackTest() {
        Mockito.doThrow(RuntimeException.class)
                .when(activityRepository)
                .save(any(Activity.class));

        var organization = new Organization("test organization");
        organizationRepository.save(organization);

        long orgId = organization.getId();

        var employee = new Employee("first", "last", organization);
        employeeRepository.save(employee);

        var response = restTemplate.postForEntity(
                PATH + "/" +orgId,
                null,
                AwardResponseDTO.class);

        assertThat(response.getBody()).extracting(AwardResponseDTO::message).isEqualTo("Given awards: 1");
        assertThat(response.getBody()).extracting(AwardResponseDTO::success).isEqualTo(true);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var transactions = sagaTransactionRepository.findAll();
                    assertThat(transactions).hasSize(1);

                    var transaction = transactions.get(0);
                    assertThat(transaction.getAwardsGiven()).isOne();
                    assertThat(transaction.getAwardsEach()).isOne();
                    assertThat(transaction.getStatus()).isEqualTo(SagaStatus.FAILED);
                });
    }
}
