package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Data
@Builder
public class Event {
    private long timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long eventId;
    private Long entityId;
}
