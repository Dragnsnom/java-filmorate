package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review create(Review review) {
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public void delete(Long id) {
        reviewStorage.delete(id);
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
