package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final EventStorage eventStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.eventStorage = eventStorage;
    }

    public Review create(Review review) {
        Review created = reviewStorage.create(review);
        eventStorage.addEvent(created.getUserId(), EventType.REVIEW, OperationType.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        Review updated = reviewStorage.update(review);
        eventStorage.addEvent(updated.getUserId(), EventType.REVIEW, OperationType.UPDATE, updated.getReviewId());
        return updated;
    }

    public void delete(Long id) {
        Review existing = reviewStorage.getById(id);
        reviewStorage.delete(id);
        eventStorage.addEvent(existing.getUserId(), EventType.REVIEW, OperationType.REMOVE, existing.getReviewId());
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getByFilmId(Long filmId, int count) {
        if (count <= 0) {
            log.debug("Запрошено count={}, возвращаем пустой список без обращения к БД", count);
            return List.of();
        }
        return reviewStorage.getByFilmId(filmId, count);
    }

    public Review addLike(Long reviewId, Long userId) {
        return reviewStorage.addLike(reviewId, userId);
    }

    public Review addDislike(Long reviewId, Long userId) {
        return reviewStorage.addDislike(reviewId, userId);
    }

    public Review removeLike(Long reviewId, Long userId) {
        return reviewStorage.removeLike(reviewId, userId);
    }

    public Review removeDislike(Long reviewId, Long userId) {
        return reviewStorage.removeDislike(reviewId, userId);
    }
}
