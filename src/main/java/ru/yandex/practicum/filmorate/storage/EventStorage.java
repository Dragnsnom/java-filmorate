package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.util.List;

public interface EventStorage {
    void addEvent(Long userId, EventType eventType, OperationType operation, Long entityId);

    List<Event> getEventsByUserId(Long userId);
}
