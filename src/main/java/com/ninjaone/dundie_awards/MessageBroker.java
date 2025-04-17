package com.ninjaone.dundie_awards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.ActivityMessageDTO;
import com.ninjaone.dundie_awards.dto.SagaTransactionResponseDTO;
import com.ninjaone.dundie_awards.exception.MessageBrokerException;
import com.ninjaone.dundie_awards.model.Activity;
import io.awspring.cloud.sqs.operations.MessagingOperationFailedException;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.List;
import java.util.concurrent.CompletionException;

import static java.util.Collections.emptyList;

@Slf4j
@Component
public class MessageBroker {

    public static final String ACTIVITY_QUEUE = "activity_queue";
    public static final String SAGA_QUEUE = "saga_response_queue";

    private final SqsTemplate sqsTemplate;
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    public MessageBroker(final SqsTemplate sqsTemplate,
                         final SqsAsyncClient sqsAsyncClient,
                         final ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(SagaTransactionResponseDTO message) throws MessageBrokerException {
        try {
            SendResult<SagaTransactionResponseDTO> result = sqsTemplate.send(SAGA_QUEUE, message);
            log.info("MessageBroker Saga message sent with id: {}", result.messageId());
        } catch (MessagingOperationFailedException e) {
            log.error("SQS server connection error, unable to send Saga message: {}", e.getMessage());
            throw new MessageBrokerException(e.getMessage());
        }
    }

    public void sendMessage(ActivityMessageDTO message) throws MessageBrokerException {
        try {
            SendResult<ActivityMessageDTO> result = sqsTemplate.send(ACTIVITY_QUEUE, message);
            log.info("MessageBroker message sent with id: {}", result.messageId());
        } catch (MessagingOperationFailedException e) {
            log.error("SQS server connection error, unable to send message: {}", e.getMessage());
            throw new MessageBrokerException(e.getMessage());
        }
    }

    public List<ActivityMessageDTO> getMessages() {
        return peek();
    }

    // Hacky way to peek SQS messages. Should not be used in production
    public List<ActivityMessageDTO> peek() {
        try {
            GetQueueUrlResponse queueUrlResponse = sqsAsyncClient.getQueueUrl(req ->
                    req.queueName(ACTIVITY_QUEUE)).join();

            String queueURL = queueUrlResponse.queueUrl();

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueURL)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .visibilityTimeout(1)
                    .build();

            log.info("Attempt to receiveMany");
            ReceiveMessageResponse response = sqsAsyncClient.receiveMessage(receiveRequest).join();

            List<ActivityMessageDTO> activities = response.messages().stream().map(this::toActivity).toList();

            log.info("Finish receiveMany: {}", activities);
            return activities;
        } catch (SqsException | CompletionException e) {
            log.error("SQS server connection error or queue do not exist yet: {}", e.getMessage());
            return emptyList();
        } catch (Exception e) {
            log.error("SQS Error", e);
            return emptyList();
        }
    }

    private ActivityMessageDTO toActivity(Message message) {
        try {
            return objectMapper.readValue(message.body(), ActivityMessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
