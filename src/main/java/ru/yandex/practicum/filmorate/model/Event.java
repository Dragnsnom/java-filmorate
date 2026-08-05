package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"timestamp", "userId", "eventType", "operation", "eventId", "entityId"})
public class Event {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long eventId;
    private Long entityId;
}
