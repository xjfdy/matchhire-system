package com.matchhire.notificationservice.listener;

import com.matchhire.notificationservice.event.ApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(topics = "application-events", groupId = "notification-service")
    public void handleApplicationEvent(ApplicationEvent event) {
        switch (event.getEventType()) {
            case "SUBMITTED" -> notifySubmitted(event);
            case "STATUS_CHANGED" -> notifyStatusChanged(event);
            default ->  log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void notifySubmitted(ApplicationEvent event) {
        log.info("[EMAIL to candidate {}] Your application (id={}) for job {} has been submitted successfully.",
                event.getCandidateId(), event.getApplicationId(), event.getJobId());
    }

    private void notifyStatusChanged(ApplicationEvent event) {
        log.info("[EMAIL to candidate {}] Your application (id={}) status changed from {} to {}.",
                event.getCandidateId(), event.getApplicationId(), event.getOldStatus(), event.getNewStatus());
    }
}
