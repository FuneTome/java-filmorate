package ru.yandex.practicum.filmorate.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.model.Review;

@Component
public class ReviewMapper {

    public Review toReview(ReviewRequest reviewRequest) {
        return Review.builder()
                .content(reviewRequest.getContent())
                .isPositive(reviewRequest.getIsPositive())
                .userId(reviewRequest.getUserId())
                .filmId(reviewRequest.getFilmId())
                .build();
    }

    public ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public Review toReview(ReviewUpdateDto reviewUpdateDto) {
        return Review.builder()
                .id(reviewUpdateDto.getReviewId())
                .content(reviewUpdateDto.getContent())
                .isPositive(reviewUpdateDto.getIsPositive())
                .userId(reviewUpdateDto.getUserId())
                .filmId(reviewUpdateDto.getFilmId())
                .build();
    }
}
