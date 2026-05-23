package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.ReactionType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage storage;
    private final ReviewMapper mapper;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public List<ReviewDto> getReviews(Long filmId, int count) throws NotFoundException {
        filmExists(filmId);
        return storage.getReviews(filmId, count).stream()
                .map(mapper::toDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public ReviewDto addReview(ReviewRequest reviewRequest) {
        log.debug("Добавление отзыва {}", reviewRequest);
        filmExists(reviewRequest.getFilmId().longValue());
        userExists(reviewRequest.getUserId().longValue());

        Review review = mapper.toReview(reviewRequest);

        return mapper.toDto(storage.addReview(review));
    }

    public ReviewDto updateReview(ReviewUpdateDto reviewUpdateDto) {
        Long reviewId = reviewUpdateDto.getReviewId();
        Long filmId = reviewUpdateDto.getFilmId().longValue();
        filmExists(filmId);

        log.debug("Обновление отзыва c id {}", reviewId);

        reviewExists(reviewId);

        Review review = mapper.toReview(reviewUpdateDto);
        return mapper.toDto(storage.updateReview(review));
    }

    public ReviewDto getReviewById(long id) {
        log.debug("Получение отзыва по id {}", id);

        Review review = storage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        return mapper.toDto(review);
    }

    public void deleteReviewById(Long reviewId) {
        reviewExists(reviewId);
        storage.deleteReviewById(reviewId);
    }

    public void addReactionOnReview(Long reviewId, Long userId, ReactionType newReactiontype) {
        log.debug("Добавление реакции на отзыв. userId {}, reviewId {}", userId, reviewId);

        userExists(userId);
        reviewExists(reviewId);

        ReactionType oldReactionTyp = storage.getReaction(reviewId, userId).orElse(null);
        int delta = calculateDelta(oldReactionTyp, newReactiontype);

        if (delta != 0) {
            storage.addReaction(reviewId, userId, newReactiontype);
            storage.updateUseful(delta, reviewId);
        }
    }

    public void removeReactionOnReview(Long reviewId, Long userId, ReactionType reactiontype) {
        log.debug("Удаление реакции с отзыва. userId {}, reviewId {}", userId, reviewId);
        userExists(userId);
        reviewExists(reviewId);

        ReactionType oldReactionTyp = storage.getReaction(reviewId, userId).orElse(null);

        if (oldReactionTyp != null) {
            if (oldReactionTyp == reactiontype) {
                if (oldReactionTyp == ReactionType.LIKE) {
                    storage.updateUseful(-1, reviewId);
                } else {
                    storage.updateUseful(1, reviewId);
                }

                storage.deleteReaction(reviewId, userId);
            }
        }
    }

    private void reviewExists(Long reviewId) {
        if (storage.getReviewById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв c id - " + reviewId + " не найден");
        }
    }

    private void userExists(Long userId) {
        if (!userStorage.findById(userId)) {
            throw new NotFoundException("Пользователь c id - " + userId + " не найден");
        }
    }

    private void filmExists(Long filmId) {
        if (!filmStorage.findById(filmId)) {
            throw new NotFoundException("Фильм c id - " + filmId + " не найден");
        }
    }

    private int calculateDelta(ReactionType oldType, ReactionType newType) {
        if (oldType == null) {
            return newType == ReactionType.LIKE ? 1 : -1;
        }

        if (oldType == newType) {
            return 0;
        }

        return newType == ReactionType.LIKE ? 2 : -2;
    }
}
