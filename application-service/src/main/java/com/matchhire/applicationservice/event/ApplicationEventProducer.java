package com.matchhire.applicationservice.event;

import com.matchhire.applicationservice.model.Application;
import com.matchhire.applicationservice.model.ApplicationStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ApplicationEventProducer {

    private static final String TOPIC = "application-events";

    private final KafkaTemplate<String, ApplicationEvent> kafkaTemplate;

    public ApplicationEventProducer(KafkaTemplate<String, ApplicationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSubmitted(Application application) {
        ApplicationEvent event = new ApplicationEvent();
        event.setEventType("SUBMITTED");
        event.setApplicationId(application.getId());
        event.setCandidateId(application.getCandidateId());
        event.setJobId(application.getJobId());
        event.setEmployerId(application.getEmployerId());
        event.setNewStatus(application.getStatus());
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(TOPIC, application.getId().toString(), event);
    }

    public void publishStatusChanged(Application application, ApplicationStatus oldStatus) {
        ApplicationEvent event = new ApplicationEvent();
        event.setEventType("STATUS_CHANGED");
        event.setApplicationId(application.getId());
        event.setCandidateId(application.getCandidateId());
        event.setJobId(application.getJobId());
        event.setEmployerId(application.getEmployerId());
        event.setOldStatus(oldStatus);
        event.setNewStatus(application.getStatus());
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(TOPIC, application.getId().toString(), event);
    }

}
