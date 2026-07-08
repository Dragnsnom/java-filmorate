package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        log.debug("Создание нового фильма в БД: {}", film.getName());
        validateMpaAndGenres(film);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);

        log.info("Фильм создан в БД: id={}, name={}", film.getId(), film.getName());
        return getFilm(film.getId());
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма в БД: id={}, name={}", film.getId(), film.getName());
        getFilm(film.getId()); // Бросит исключение, если не найден
        validateMpaAndGenres(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);

        log.info("Фильм обновлен в БД: id={}, name={}", film.getId(), film.getName());
        return getFilm(film.getId());
    }

    @Override
    public List<Film> getAll() {
        log.debug("Получение всех фильмов из БД");
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        for (Film film : films) {
            loadFilmDetails(film);
        }
        return films;
    }

    @Override
    public Film getFilm(Long id) {
        log.trace("Получение фильма по id: {}", id);
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film film = films.get(0);
        loadFilmDetails(film);
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        log.debug("Добавление лайка в БД: filmId={}, userId={}", film.getId(), user.getId());
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, film.getId(), user.getId());
        if (count != null && count > 0) {
            throw new DuplicateLikeException("Пользователь уже поставил лайк этому фильму");
        }

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, film.getId(), user.getId());
        log.info("Лайк добавлен в БД: filmId={}, userId={}", film.getId(), user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        log.debug("Удаление лайка из БД: filmId={}, userId={}", film.getId(), user.getId());
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, film.getId(), user.getId());
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, film.getId(), user.getId());
        log.info("Лайк удален из БД: filmId={}, userId={}", film.getId(), user.getId());
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        log.debug("Запрос популярных фильмов из БД: count={}", count);
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC, f.id ASC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        for (Film film : films) {
            loadFilmDetails(film);
        }
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        // Deduplicate genres by ID keeping order if possible
        List<Genre> uniqueGenres = film.getGenres().stream()
                .filter(genre -> genre != null)
                .collect(Collectors.toMap(Genre::getId, g -> g, (g1, g2) -> g1, java.util.LinkedHashMap::new))
                .values().stream().toList();

        List<Object[]> batchArgs = uniqueGenres.stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void loadFilmDetails(Film film) {
        String genresSql = "SELECT g.* FROM genres g " +
                "INNER JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(genresSql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        String likesSql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        int mpaId = rs.getInt("mpa_rating_id");
        if (!rs.wasNull()) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
        }
        return film;
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не существует");
            }
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
            for (Genre genre : film.getGenres()) {
                if (genre == null) {
                    continue;
                }
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genre.getId());
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не существует");
                }
            }
        }
    }
}
