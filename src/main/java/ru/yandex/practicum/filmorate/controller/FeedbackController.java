package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Feedback;
import ru.yandex.practicum.filmorate.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public Feedback create(@Valid @RequestBody Feedback feedback) {
        return feedbackService.create(feedback);
    }

    @PutMapping
    public Feedback update(@Valid @RequestBody Feedback feedback) {
        return feedbackService.update(feedback);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        feedbackService.delete(id);
    }

    @GetMapping("/{id}")
    public Feedback getById(@PathVariable Long id) {
        return feedbackService.getById(id);
    }

    @GetMapping
    public List<Feedback> getByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return feedbackService.getByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Feedback addLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return feedbackService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Feedback addDislike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return feedbackService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Feedback removeLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return feedbackService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Feedback removeDislike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return feedbackService.removeDislike(id, userId);
    }
}
