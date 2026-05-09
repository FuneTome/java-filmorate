package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<GenreDto> findAll() {
        return jdbcTemplate.query("SELECT genre_id, name FROM Genre ORDER BY genre_id", genreRowMapper);
    }

    public Optional<GenreDto> findById(int id) {
        try {
            GenreDto genre = jdbcTemplate.queryForObject(
                    "SELECT genre_id, name FROM Genre WHERE genre_id = ?",
                    genreRowMapper,
                    id
            );
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}