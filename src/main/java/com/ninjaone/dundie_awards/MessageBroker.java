package com.ninjaone.dundie_awards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.model.Activity;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

@Slf4j
@Component
public class MessageBroker {

    public static final String ACTIVITY_QUEUE = "activity_queue";
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

    public void sendMessage(Activity message) {
        SendResult<Object> result = sqsTemplate.send(ACTIVITY_QUEUE, message);
        log.info("MessageBroker message sent with id: {}", result.messageId());
    }

    public List<Activity> getMessages() {
        return peek();
    }

    // Hacky way to peek SQS messages. Should not be used in production
    public List<Activity> peek() {
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

        List<Activity> activities = response.messages().stream().map(this::toActivity).toList();

        log.info("Finish receiveMany: {}", activities);
        return activities;
    }

    private Activity toActivity(Message message) {
        try {
            return objectMapper.readValue(message.body(), Activity.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
