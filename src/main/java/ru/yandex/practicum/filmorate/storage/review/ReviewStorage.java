package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.ReactionType;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    List<Review> getReviews(Long filmId, int count);

    Review addReview(Review review);

    Optional<Review> getReviewById(Long id);

    boolean deleteReviewById(Long id);

    Review updateReview(Review review);

    void addReaction(Long reviewId, Long userId, ReactionType type);

    Optional<ReactionType> getReaction(Long reviewId, Long userId);

    void deleteReaction(Long reviewId, Long userId);

    void updateUseful(int delta, Long reviewId);
}
