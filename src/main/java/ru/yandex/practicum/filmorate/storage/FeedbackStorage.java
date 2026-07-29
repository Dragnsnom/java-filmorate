package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feedback;

import java.util.List;

public interface FeedbackStorage {
    Feedback create(Feedback feedback);

    Feedback update(Feedback feedback);

    void delete(Long id);

    Feedback getById(Long id);

    List<Feedback> getByFilmId(Long filmId, int count);

    Feedback addLike(Long reviewId, Long userId);

    Feedback addDislike(Long reviewId, Long userId);

    Feedback removeLike(Long reviewId, Long userId);

    Feedback removeDislike(Long reviewId, Long userId);
}
