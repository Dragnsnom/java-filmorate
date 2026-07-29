package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feedback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component("feedbackDbStorage")
@Primary
public class FeedbackDbStorage implements FeedbackStorage {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Feedback create(Feedback feedback) {
        log.debug("Создание отзыва в БД: filmId={}, userId={}", feedback.getFilmId(), feedback.getUserId());
        checkFilmExists(feedback.getFilmId());
        checkUserExists(feedback.getUserId());

        String sql = "INSERT INTO reviews (film_id, user_id, content, is_positive, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, feedback.getFilmId());
            ps.setLong(2, feedback.getUserId());
            ps.setString(3, feedback.getContent());
            ps.setBoolean(4, feedback.getIsPositive());
            return ps;
        }, keyHolder);

        feedback.setReviewId(keyHolder.getKey().longValue());
        log.info("Отзыв создан в БД: id={}", feedback.getReviewId());
        return getById(feedback.getReviewId());
    }

    @Override
    public Feedback update(Feedback feedback) {
        log.debug("Обновление отзыва в БД: id={}", feedback.getReviewId());
        getById(feedback.getReviewId());

        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                feedback.getContent(),
                feedback.getIsPositive(),
                feedback.getReviewId());

        log.info("Отзыв обновлён в БД: id={}", feedback.getReviewId());
        return getById(feedback.getReviewId());
    }

    @Override
    public void delete(Long id) {
        log.debug("Удаление отзыва из БД: id={}", id);
        getById(id);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
        log.info("Отзыв удалён из БД: id={}", id);
    }

    @Override
    public Feedback getById(Long id) {
        log.trace("Получение отзыва по id: {}", id);
        String sql = "SELECT * FROM reviews WHERE id = ?";
        List<Feedback> feedbacks = jdbcTemplate.query(sql, this::mapRowToFeedback, id);
        if (feedbacks.isEmpty()) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return feedbacks.get(0);
    }

    @Override
    public List<Feedback> getByFilmId(Long filmId, int count) {
        log.debug("Получение отзывов из БД: filmId={}, count={}", filmId, count);
        if (filmId == null) {
            String sql = "SELECT * FROM reviews ORDER BY useful DESC, id ASC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToFeedback, count);
        }
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC, id ASC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToFeedback, filmId, count);
    }

    @Override
    public Feedback addLike(Long reviewId, Long userId) {
        log.debug("Лайк отзыву в БД: reviewId={}, userId={}", reviewId, userId);
        setEstimation(reviewId, userId, true);
        return getById(reviewId);
    }

    @Override
    public Feedback addDislike(Long reviewId, Long userId) {
        log.debug("Дизлайк отзыву в БД: reviewId={}, userId={}", reviewId, userId);
        setEstimation(reviewId, userId, false);
        return getById(reviewId);
    }

    @Override
    public Feedback removeLike(Long reviewId, Long userId) {
        log.debug("Удаление лайка отзыва в БД: reviewId={}, userId={}", reviewId, userId);
        removeEstimation(reviewId, userId, true);
        return getById(reviewId);
    }

    @Override
    public Feedback removeDislike(Long reviewId, Long userId) {
        log.debug("Удаление дизлайка отзыва в БД: reviewId={}, userId={}", reviewId, userId);
        removeEstimation(reviewId, userId, false);
        return getById(reviewId);
    }

    private void setEstimation(Long reviewId, Long userId, boolean isUseful) {
        getById(reviewId);
        checkUserExists(userId);

        String sql = "MERGE INTO review_likes (review_id, user_id, is_useful) KEY (review_id, user_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, isUseful);
        recalcUseful(reviewId);
    }

    private void removeEstimation(Long reviewId, Long userId, boolean isUseful) {
        getById(reviewId);

        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_useful = ?";
        jdbcTemplate.update(sql, reviewId, userId, isUseful);
        recalcUseful(reviewId);
    }

    private void recalcUseful(Long reviewId) {
        String sql = "UPDATE reviews SET useful = (" +
                "SELECT COALESCE(SUM(CASE WHEN is_useful THEN 1 ELSE -1 END), 0) " +
                "FROM review_likes WHERE review_id = ?) WHERE id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    private Feedback mapRowToFeedback(ResultSet rs, int rowNum) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setReviewId(rs.getLong("id"));
        feedback.setFilmId(rs.getLong("film_id"));
        feedback.setUserId(rs.getLong("user_id"));
        feedback.setContent(rs.getString("content"));
        feedback.setIsPositive(rs.getBoolean("is_positive"));
        feedback.setUseful(rs.getInt("useful"));
        return feedback;
    }

    private void checkFilmExists(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    private void checkUserExists(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }
}
