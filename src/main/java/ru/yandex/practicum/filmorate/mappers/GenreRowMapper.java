package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreDto> {
    @Override
    public GenreDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new GenreDto(
                rs.getInt("genre_id"),
                rs.getString("name")
        );
    }
}