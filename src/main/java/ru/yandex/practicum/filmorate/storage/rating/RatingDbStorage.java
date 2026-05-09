package ru.yandex.practicum.filmorate.storage.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mappers.RatingRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    public List<MpaDto> findAll() {
        return jdbcTemplate.query("SELECT rating_id, name FROM Rating ORDER BY rating_id", ratingRowMapper);
    }

    public Optional<MpaDto> findById(int id) {
        try {
            MpaDto rating = jdbcTemplate.queryForObject(
                    "SELECT rating_id, name FROM Rating WHERE rating_id = ?",
                    ratingRowMapper,
                    id
            );
            return Optional.ofNullable(rating);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}