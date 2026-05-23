package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful")
        );
    }
}
