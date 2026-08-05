package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component("reviewDbStorage")
@Primary
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review create(Review review) {
        log.debug("Создание отзыва в БД: filmId={}, userId={}", review.getFilmId(), review.getUserId());
        checkFilmExists(review.getFilmId());
        checkUserExists(review.getUserId());

        String sql = "INSERT INTO reviews (film_id, user_id, content, is_positive, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, review.getFilmId());
            ps.setLong(2, review.getUserId());
            ps.setString(3, review.getContent());
            ps.setBoolean(4, review.getIsPositive());
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        log.info("Отзыв создан в БД: id={}", review.getReviewId());
        return getById(review.getReviewId());
    }

    @Override
    public Review update(Review review) {
        log.debug("Обновление отзыва в БД: id={}", review.getReviewId());
        getById(review.getReviewId());

        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        log.info("Отзыв обновлён в БД: id={}", review.getReviewId());
        return getById(review.getReviewId());
    }

    @Override
    public void delete(Long id) {
        log.debug("Удаление отзыва из БД: id={}", id);
        getById(id);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
        log.info("Отзыв удалён из БД: id={}", id);
    }

    @Override
    public Review getById(Long id) {
        log.trace("Получение отзыва по id: {}", id);
        String sql = "SELECT * FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, id);
        if (reviews.isEmpty()) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return reviews.get(0);
    }

    @Override
    public List<Review> getByFilmId(Long filmId, int count) {
        log.debug("Получение отзывов из БД: filmId={}, count={}", filmId, count);
        if (filmId == null) {
            String sql = "SELECT * FROM reviews ORDER BY useful DESC, id ASC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToReview, count);
        }
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC, id ASC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public Review addLike(Long reviewId, Long userId) {
        log.debug("Лайк отзыву в БД: reviewId={}, userId={}", reviewId, userId);
        setEstimation(reviewId, userId, true);
        return getById(reviewId);
    }

    @Override
    public Review addDislike(Long reviewId, Long userId) {
        log.debug("Дизлайк отзыву в БД: reviewId={}, userId={}", reviewId, userId);
        setEstimation(reviewId, userId, false);
        return getById(reviewId);
    }

    @Override
    public Review removeLike(Long reviewId, Long userId) {
        log.debug("Удаление лайка отзыва в БД: reviewId={}, userId={}", reviewId, userId);
        removeEstimation(reviewId, userId, true);
        return getById(reviewId);
    }

    @Override
    public Review removeDislike(Long reviewId, Long userId) {
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

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUseful(rs.getInt("useful"));
        return review;
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
