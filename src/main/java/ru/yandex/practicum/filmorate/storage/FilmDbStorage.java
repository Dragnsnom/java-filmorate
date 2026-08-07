package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
    @Transactional
    public Film update(Film film) {
        log.debug("Обновление фильма в БД: id={}, name={}", film.getId(), film.getName());
        getFilm(film.getId()); // Бросит исключение, если не найден
        validateMpaAndGenres(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                " WHERE id = ?";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());

        saveGenres(film);
        saveDirectors(film);

        log.info("Фильм обновлен в БД: id={}, name={}", film.getId(), film.getName());
        return getFilm(film.getId());
    }

    @Override
    public List<Film> getAll() {
        log.debug("Получение всех фильмов из БД");
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id ";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        loadFilmDetails(films);
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
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.trace("Получение фильмов по пересечению \n userId: {} \n friendId: {}", userId, friendId);

        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ? " +
                "INTERSECT SELECT film_id FROM film_likes WHERE user_id = ?) " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC, f.id ASC";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);
        loadFilmDetails(films);
        return films;
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
    public List<Film> getPopularFilms(Long count, Integer genreId, Integer year) {
        log.debug("Запрос популярных фильмов из БД: count={}, genreId={}, year={}", count, genreId, year);

        StringBuilder sql = new StringBuilder(
                "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id "
        );

        List<Object> args = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.id = fg.film_id ");
        }

        sql.append("WHERE 1 = 1 ");

        if (genreId != null) {
            sql.append("AND fg.genre_id = ? ");
            args.add(genreId);
        }

        if (year != null) {
            sql.append("AND YEAR(f.release_date) = ? ");
            args.add(year);
        }

        sql.append(
                "GROUP BY f.id, m.name " +
                        "ORDER BY like_count DESC, f.id ASC " +
                        "LIMIT ?"
        );

        args.add(count);

        List<Film> films = jdbcTemplate.query(
                sql.toString(),
                this::mapRowToFilm,
                args.toArray()
        );

        loadFilmDetails(films);

        return films;
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        log.debug("Получение фильмов режиссёра с id={}, сортировка={}", directorId, sortBy);

        String orderBy;

        if ("year".equals(sortBy)) {
            orderBy = "f.release_date ASC, f.id ASC";
        } else if ("likes".equals(sortBy)) {
            orderBy = "likes_count DESC, f.id ASC";
        } else {
            throw new ValidationException("Параметр sortBy должен иметь значение year или likes");
        }

        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " + "FROM films f " +
                "JOIN film_directors fd ON fd.film_id = f.id " +
                "LEFT JOIN mpa_ratings m ON m.id = f.mpa_rating_id " +
                "LEFT JOIN film_likes fl ON fl.film_id = f.id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.id, " +
                "f.name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.mpa_rating_id, " +
                "m.name ORDER BY " + orderBy;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
        loadFilmDetails(films);
        return films;
    }

    @Override
    public List<Film> searchByTitle(String query) {
        log.debug("Поиск фильмов по названию в БД: query={}", query);
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE LOWER(f.name) LIKE LOWER(?) " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC, f.id ASC";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, "%" + query + "%");
        loadFilmDetails(films);
        return films;
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        log.debug("Поиск рекомендаций для пользователя: id={}", userId);
        String findSimilarUserSql = "SELECT user_id FROM film_likes " +
                "WHERE film_id IN (SELECT film_id FROM film_likes WHERE user_id = ?) " +
                "AND user_id != ? " +
                "GROUP BY user_id " +
                "ORDER BY COUNT(film_id) DESC " +
                "LIMIT 1";

        List<Long> similarUsers = jdbcTemplate.queryForList(findSimilarUserSql, Long.class, userId, userId);

        if (similarUsers.isEmpty()) {
            return List.of();
        }

        Long similarUserId = similarUsers.get(0);

        String recommendedFilmsSql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "INNER JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE fl.user_id = ? " +
                "AND f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)";

        List<Film> recommendations = jdbcTemplate.query(recommendedFilmsSql, this::mapRowToFilm, similarUserId, userId);
        loadFilmDetails(recommendations);

        return recommendations;
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

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{
                        film.getId(),
                        director.getId()
                })
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void loadFilmDetails(Film film) {
        loadFilmDetails(List.of(film));
    }

    private void loadFilmDetails(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
            film.setLikes(new HashSet<>());
            film.setDirectors(new LinkedHashSet<>());
        }

        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String inPlaceholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String genresSql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name FROM genres g " +
                "INNER JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inPlaceholders + ") " +
                "ORDER BY g.id";

        Object[] args = filmIds.toArray();

        java.util.Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        jdbcTemplate.query(genresSql, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
                film.getGenres().add(genre);
            }
        }, args);

        String likesSql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inPlaceholders + ")";
        jdbcTemplate.query(likesSql, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getLikes().add(rs.getLong("user_id"));
            }
        }, args);

        String directorsSql = "SELECT fd.film_id, d.id AS director_id, d.name AS director_name " +
                "FROM film_directors fd JOIN directors d ON d.id = fd.director_id " +
                "WHERE fd.film_id IN (" + inPlaceholders + ") ORDER BY fd.film_id, d.id";
        jdbcTemplate.query(directorsSql, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);

            if (film != null) {
                film.getDirectors().add(new Director(
                        rs.getInt("director_id"),
                        rs.getString("director_name")
                ));
            }
        }, args);

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
            List<Integer> genreIds = film.getGenres().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Genre::getId)
                    .distinct()
                    .toList();

            if (!genreIds.isEmpty()) {
                String placeholders = genreIds.stream().map(id -> "?").collect(Collectors.joining(","));
                String sql = "SELECT id FROM genres WHERE id IN (" + placeholders + ")";
                List<Integer> existingIds = jdbcTemplate.queryForList(sql, Integer.class, genreIds.toArray());
                for (Integer id : genreIds) {
                    if (!existingIds.contains(id)) {
                        throw new NotFoundException("Жанр с id=" + id + " не существует");
                    }
                }
            }
        }
    }
}
