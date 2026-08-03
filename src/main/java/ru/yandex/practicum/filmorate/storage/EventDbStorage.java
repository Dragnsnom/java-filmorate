package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Event> eventRowMapper() {
        return (rs, rowNum) -> Event.builder()
                .eventId(rs.getLong("event_id"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(OperationType.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .build();
    }

    @Override
    public void addEvent(Long userId, EventType eventType, OperationType operation, Long entityId) {
        String sql = "INSERT INTO events(timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long timestamp = Instant.now().toEpochMilli();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"event_id"});
            stmt.setLong(1, timestamp);
            stmt.setLong(2, userId);
            stmt.setString(3, eventType.name());
            stmt.setString(4, operation.name());
            stmt.setLong(5, entityId);
            return stmt;
        }, keyHolder);
    }

    @Override
    public List<Event> getEventsByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_id ASC";
        return jdbcTemplate.query(sql, eventRowMapper(), userId);
    }
}
