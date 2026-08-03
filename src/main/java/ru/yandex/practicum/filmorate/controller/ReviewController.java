package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    private final EventStorage eventStorage;
    private final Map<Long, Review> reviews = new HashMap<>();
    private final AtomicLong currentId = new AtomicLong(0);

    public ReviewController(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        long id = currentId.incrementAndGet();
        review.setReviewId(id);
        review.setUseful(0);
        reviews.put(id, review);
        
        eventStorage.addEvent(review.getUserId(), EventType.REVIEW, OperationType.ADD, id);
        
        return review;
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        Review existing = reviews.get(review.getReviewId());
        if (existing != null) {
            existing.setContent(review.getContent());
            existing.setIsPositive(review.getIsPositive());
            review = existing;
        } else {
            reviews.put(review.getReviewId(), review);
        }
        
        eventStorage.addEvent(review.getUserId(), EventType.REVIEW, OperationType.UPDATE, review.getReviewId());
        
        return review;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        Review existing = reviews.remove(id);
        if (existing != null) {
            eventStorage.addEvent(existing.getUserId(), EventType.REVIEW, OperationType.REMOVE, existing.getReviewId());
        }
    }
}
