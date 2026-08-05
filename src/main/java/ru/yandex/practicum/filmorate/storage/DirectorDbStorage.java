package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("directorDbStorage")
@Primary
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getAllDirectors() {
        log.debug("Получение всех режиссеров из БД");
        String sql = "SELECT * FROM directors";

        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    @Override
    public Director getDirector(Long id) {
        log.trace("Поиск пользователя в БД по id: {}", id);
        return findDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id=" + id + " не найден"));
    }

    @Override
    public Director addDirector(Director director) {
        log.debug("Создание нового режиссера в БД: name={}", director.getName());
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        log.info("Режиссер создан в БД: id={}, name={}", director.getId(), director.getName());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        log.debug("Обновление режиссера в БД: id={}, name={}", director.getId(), director.getName());
        checkDirectorExists(director.getId());

        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                director.getName(),
                director.getId());

        log.info("Режиссер обновлен в БД: id={}, name={}", director.getId(), director.getName());
        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public Optional<Director> findDirectorById(long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, this::mapRowToDirector, id);
        if (directors.isEmpty()) {
            return Optional.empty();
        }
        Director director = directors.get(0);
        return Optional.of(director);
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    }

    private void checkDirectorExists(int id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Режиссер с id=" + id + " не найден");
        }
    }
}
