package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    @Override
    public List<Event> getUserFeed(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM events e WHERE e.user_id = ? ORDER BY event_id", eventRowMapper, id
        );
    }

    @Override
    public void createEvent(Event event) {
        String sql = "INSERT INTO events (event_timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }
}
