package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RatingRowMapper implements RowMapper<MpaDto> {
    @Override
    public MpaDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MpaDto(
                rs.getInt("rating_id"),
                rs.getString("name")
        );
    }
}