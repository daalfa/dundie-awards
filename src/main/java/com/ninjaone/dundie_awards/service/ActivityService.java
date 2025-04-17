package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityMessageDTO;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.event.ActivityEvent;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.ninjaone.dundie_awards.MessageBroker.ACTIVITY_QUEUE;

@Slf4j
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final ApplicationEventPublisher eventPublisher;

    public ActivityService(
            final ActivityRepository activityRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.activityRepository = activityRepository;
        this.eventPublisher = eventPublisher;
    }

    @SqsListener(queueNames = ACTIVITY_QUEUE, acknowledgementMode = "ALWAYS")
    @Transactional
    protected void listener(ActivityMessageDTO message) {
        log.info("You've Got Mail! {}", message);

        ActivityEvent activityEvent = new ActivityEvent(message.transactionSagaId());
        eventPublisher.publishEvent(activityEvent);

        Activity activity = new Activity(message.occuredAt(), message.event());
        activityRepository.save(activity);
    }
}
