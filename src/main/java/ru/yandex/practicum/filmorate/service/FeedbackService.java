package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feedback;
import ru.yandex.practicum.filmorate.storage.FeedbackStorage;

import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackStorage feedbackStorage;

    public FeedbackService(@Qualifier("feedbackDbStorage") FeedbackStorage feedbackStorage) {
        this.feedbackStorage = feedbackStorage;
    }

    public Feedback create(Feedback feedback) {
        return feedbackStorage.create(feedback);
    }

    public Feedback update(Feedback feedback) {
        return feedbackStorage.update(feedback);
    }

    public void delete(Long id) {
        feedbackStorage.delete(id);
    }

    public Feedback getById(Long id) {
        return feedbackStorage.getById(id);
    }

    public List<Feedback> getByFilmId(Long filmId, int count) {
        return feedbackStorage.getByFilmId(filmId, count);
    }

    public Feedback addLike(Long reviewId, Long userId) {
        return feedbackStorage.addLike(reviewId, userId);
    }

    public Feedback addDislike(Long reviewId, Long userId) {
        return feedbackStorage.addDislike(reviewId, userId);
    }

    public Feedback removeLike(Long reviewId, Long userId) {
        return feedbackStorage.removeLike(reviewId, userId);
    }

    public Feedback removeDislike(Long reviewId, Long userId) {
        return feedbackStorage.removeDislike(reviewId, userId);
    }
}
