package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.ReactionType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
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
    private final EventStorage eventStorage;

    public List<ReviewDto> getReviews(Long filmId, int count) throws NotFoundException {
        filmExists(filmId);
        return storage.getReviews(filmId, count).stream()
                .map(mapper::toDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public ReviewDto addReview(ReviewRequest reviewRequest) {
        log.debug("Добавление отзыва {}", reviewRequest);
        filmExists(reviewRequest.getFilmId());
        userExists(reviewRequest.getUserId());

        Review review = mapper.toReview(reviewRequest);

        ReviewDto returnedReview = mapper.toDto(storage.addReview(review));

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.ADD)
                .entityId(review.getId())
                .build();

        eventStorage.createEvent(event);

        return returnedReview;
    }

    public ReviewDto updateReview(ReviewUpdateDto reviewUpdateDto) {
        Long reviewId = reviewUpdateDto.getReviewId();
        Long filmId = reviewUpdateDto.getFilmId();
        filmExists(filmId);

        log.debug("Обновление отзыва c id {}", reviewId);

        reviewExists(reviewId);

        Review review = mapper.toReview(reviewUpdateDto);
        ReviewDto returnedReview = mapper.toDto(storage.updateReview(review));

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.UPDATE)
                .entityId(review.getId())
                .build();

        eventStorage.createEvent(event);

        return returnedReview;
    }

    public ReviewDto getReviewById(long reviewId) {
        log.debug("Получение отзыва по id {}", reviewId);

        Review review = storage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        return mapper.toDto(review);
    }

    public void deleteReviewById(Long reviewId) {
        reviewExists(reviewId);
        ReviewDto review = getReviewById(reviewId);
        storage.deleteReviewById(reviewId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.REMOVE)
                .entityId(review.getReviewId())
                .build();

        eventStorage.createEvent(event);
    }

    public void addReactionOnReview(Long reviewId, Long userId, ReactionType newReactiontype) {
        log.debug("Добавление реакции на отзыв. userId {}, reviewId {}", userId, reviewId);

        userExists(userId);
        reviewExists(reviewId);

        ReactionType oldReactionTyp = storage.getReaction(reviewId, userId).orElse(null);
        int delta = calculateReviewUsefulDelta(oldReactionTyp, newReactiontype);

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

    private int calculateReviewUsefulDelta(ReactionType oldType, ReactionType newType) {
        if (oldType == null) {
            return newType == ReactionType.LIKE ? 1 : -1;
        }

        if (oldType == newType) {
            return 0;
        }

        return newType == ReactionType.LIKE ? 2 : -2;
    }
}
