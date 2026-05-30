package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.model.ReactionType;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    public List<ReviewDto> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return service.getReviews(filmId, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto addReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        return service.addReview(reviewRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("id") long reviewId) {
        service.deleteReviewById(reviewId);
    }

    @PutMapping
    public ReviewDto updateReview(@Valid @RequestBody ReviewUpdateDto reviewUpdateDto) {
        return service.updateReview(reviewUpdateDto);
    }

    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable("id") long reviewId) {
        return service.getReviewById(reviewId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeOnReview(@PathVariable("id") long reviewId, @PathVariable long userId) {
        service.addReactionOnReview(reviewId, userId, ReactionType.LIKE);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeOnReview(@PathVariable("id") long reviewId, @PathVariable long userId) {
        service.addReactionOnReview(reviewId, userId, ReactionType.DISLIKE);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeOnReview(@PathVariable("id") long reviewId, @PathVariable long userId) {
        service.removeReactionOnReview(reviewId, userId, ReactionType.LIKE);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeOnReview(@PathVariable("id") long reviewId, @PathVariable long userId) {
        service.removeReactionOnReview(reviewId, userId, ReactionType.DISLIKE);
    }
}
