package com.ninjaone.dundie_awards.integration;

import com.ninjaone.dundie_awards.MessageBroker;
import com.ninjaone.dundie_awards.dto.ActivityMessageDTO;
import com.ninjaone.dundie_awards.dto.SagaTransactionResponseDTO;
import com.ninjaone.dundie_awards.exception.MessageBrokerException;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.service.AwardService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


public class MessageBrokerIntegrationTest extends BaseIntegrationTest {

    @MockitoSpyBean
    MessageBroker messageBroker;

    @MockitoBean
    private ActivityRepository activityRepositoryMock;

    @MockitoBean
    private AwardService awardService;

    @Test
    void testSendActivityMessageToQueueAndConfirmTransaction() throws MessageBrokerException {
        Mockito.doNothing()
                .when(messageBroker)
                .sendMessage(any(SagaTransactionResponseDTO.class));

        var sagaResponseCaptor = ArgumentCaptor.forClass(SagaTransactionResponseDTO.class);

        messageBroker.sendMessage(new ActivityMessageDTO(LocalDateTime.now(), "test", "1"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Mockito.verify(activityRepositoryMock, times(1)).save(any());

                    Mockito.verify(messageBroker).sendMessage(sagaResponseCaptor.capture());
                    SagaTransactionResponseDTO capturedArgument = sagaResponseCaptor.getValue();
                    assertThat(capturedArgument.isSagaCompleted()).isTrue();
                });
    }

    @Test
    void testSendActivityMessageToQueueAndRollbackTransaction() throws MessageBrokerException {
        when(activityRepositoryMock.save(any())).thenThrow(RuntimeException.class);

        Mockito.doNothing()
                .when(messageBroker)
                .sendMessage(any(SagaTransactionResponseDTO.class));

        var sagaResponseCaptor = ArgumentCaptor.forClass(SagaTransactionResponseDTO.class);

        messageBroker.sendMessage(new ActivityMessageDTO(LocalDateTime.now(), "test", "1"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Mockito.verify(activityRepositoryMock, times(1)).save(any());

                    Mockito.verify(messageBroker).sendMessage(sagaResponseCaptor.capture());
                    SagaTransactionResponseDTO capturedArgument = sagaResponseCaptor.getValue();
                    assertThat(capturedArgument.isSagaCompleted()).isFalse();
                });
    }

    @Test
    void testSendSagaResponseMessageToQueueAndConfirmTransaction() throws MessageBrokerException {
        messageBroker.sendMessage(new SagaTransactionResponseDTO("id", true));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Mockito.verify(awardService, times(1)).confirmAwardTransaction(any());
                    Mockito.verify(awardService, never()).processAwardRollback(any());
                });
    }

    @Test
    void testSendSagaResponseMessageToQueueAndRollbackTransaction() throws MessageBrokerException {
        messageBroker.sendMessage(new SagaTransactionResponseDTO("id", false));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Mockito.verify(awardService, never()).confirmAwardTransaction(any());
                    Mockito.verify(awardService, times(1)).processAwardRollback(any());
                });
    }
}
