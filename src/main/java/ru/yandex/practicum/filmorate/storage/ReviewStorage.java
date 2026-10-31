package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Review getById(Long id);

    List<Review> getByFilmId(Long filmId, int count);

    Review addLike(Long reviewId, Long userId);

    Review addDislike(Long reviewId, Long userId);

    Review removeLike(Long reviewId, Long userId);

    Review removeDislike(Long reviewId, Long userId);
}
