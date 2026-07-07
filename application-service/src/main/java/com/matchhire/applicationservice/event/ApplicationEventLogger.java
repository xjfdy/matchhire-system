package com.matchhire.applicationservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Listen events
@Component
public class ApplicationEventLogger {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventLogger.class);

    @KafkaListener(topics = "application-events", groupId = "application-service")
    public void listen(ApplicationEvent event) {
        log.info("Received event: type={}, applicationId={}, jobId={}, oldStatus={}, newStatus={}",
                event.getEventType(),
                event.getApplicationId(),
                event.getJobId(),
                event.getOldStatus(),
                event.getNewStatus());
    }
}
