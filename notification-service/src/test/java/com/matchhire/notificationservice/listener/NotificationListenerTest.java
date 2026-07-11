package com.matchhire.notificationservice.listener;

import com.matchhire.notificationservice.event.ApplicationEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationListenerTest {

    private final NotificationListener notificationListener = new NotificationListener();

    @Test
    void handleApplicationEvent_withSubmittedType_doesNotThrow() {
        ApplicationEvent event = buildEvent("SUBMITTED", null, "PENDING");

        assertDoesNotThrow(() -> notificationListener.handleApplicationEvent(event));
    }

    @Test
    void handleApplicationEvent_withStatusChangedType_doesNotThrow() {
        ApplicationEvent event = buildEvent("STATUS_CHANGED", "PENDING", "REVIEWED");

        assertDoesNotThrow(() -> notificationListener.handleApplicationEvent(event));
    }

    @Test
    void handleApplicationEvent_withUnknownType_doesNotThrow() {
        ApplicationEvent event = buildEvent("SOME_UNKNOWN_TYPE", null, null);

        assertDoesNotThrow(() -> notificationListener.handleApplicationEvent(event));
    }

    private ApplicationEvent buildEvent(String eventType, String oldStatus, String newStatus) {
        ApplicationEvent event = new ApplicationEvent();
        event.setEventType(eventType);
        event.setApplicationId(UUID.randomUUID());
        event.setCandidateId(UUID.randomUUID());
        event.setJobId(UUID.randomUUID());
        event.setEmployerId(UUID.randomUUID());
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}