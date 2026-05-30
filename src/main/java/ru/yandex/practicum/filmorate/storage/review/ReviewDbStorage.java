package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.ReactionType;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate parameterJdbc;
    private final ReviewRowMapper mapper;

    private static final int DEFAULT_USEFUL = 0;
    private static final String INSERT_REVIEW =
            "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_REVIEW_BY_ID =
            "SELECT * FROM reviews WHERE review_id = ?";
    private static final String SELECT_REVIEWS = """
                    SELECT * FROM reviews
                    WHERE (:film_id IS NULL OR film_id = :film_id)
                    ORDER BY useful DESC
                    LIMIT :count
                    """;
    private static final String DELETE_REVIEW_BY_ID =
            "DELETE FROM reviews WHERE review_id = ?";
    private static final String INSERT_REACTION =
            "MERGE INTO review_reactions (review_id, user_id, reaction_type) VALUES (?, ?, ?)";
    private static final String DELETE_REACTION =
            "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String SELECT_REACTION_BY_IDS =
            "SELECT reaction_type FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String UPDATE_USEFUL = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    @Override
    public List<Review> getReviews(Long filmId, int count) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", filmId);
        params.addValue("count", count);

        return parameterJdbc.query(SELECT_REVIEWS, params, mapper);
    }

    @Override
    public Review addReview(Review review) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setLong(5, DEFAULT_USEFUL);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key != null) {
            review.setId(key.longValue());
            review.setUseful(DEFAULT_USEFUL);
            return review;
        } else {
            throw new RuntimeException("Failed to save data");
        }
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        List<Review> result = jdbc.query(
                SELECT_REVIEW_BY_ID,
                mapper,
                id
        );
        return result.stream().findFirst();
    }

    @Override
    public boolean deleteReviewById(Long id) {
        return jdbc.update(DELETE_REVIEW_BY_ID, id) > 0;
    }

    @Override
    public Review updateReview(Review review) {
        Long id = review.getId();

        StringBuilder sql = new StringBuilder("UPDATE reviews SET ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> updates = new ArrayList<>();

        if (review.getIsPositive() != null) {
            updates.add("is_positive = :is_positive");
            params.addValue("is_positive", review.getIsPositive());
        }

        if (review.getContent() != null) {
            updates.add("content = :content");
            params.addValue("content", review.getContent());
        }

        if (updates.isEmpty()) {
            throw new ValidationException("Нет полей для обновления");
        }

        sql.append(String.join(", ", updates));
        sql.append(" WHERE review_id = :id");
        params.addValue("id", id);

        parameterJdbc.update(sql.toString(), params);

        return getReviewById(id).orElseThrow(() -> new RuntimeException("Ошибка обновления отзыва"));
    }

    @Override
    public void addReaction(Long reviewId, Long userId, ReactionType type) {
        jdbc.update(INSERT_REACTION, reviewId, userId, type.toString());
    }

    @Override
    public void deleteReaction(Long reviewId, Long userId) {
        jdbc.update(DELETE_REACTION, reviewId, userId);
    }

    @Override
    public Optional<ReactionType> getReaction(Long reviewId, Long userId) {
        List<ReactionType> result = jdbc.query(
                SELECT_REACTION_BY_IDS,
                (rs, rowNum) -> ReactionType.valueOf(rs.getString("reaction_type")),
                reviewId,
                userId
        );
        return result.stream().findFirst();
    }

    @Override
    public void updateUseful(int delta, Long reviewId) {
        jdbc.update(UPDATE_USEFUL, delta, reviewId);
    }
}
