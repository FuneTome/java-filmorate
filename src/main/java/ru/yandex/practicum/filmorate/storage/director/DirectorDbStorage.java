package ru.yandex.practicum.filmorate.storage.director;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    private static final String INSERT_DIRECTOR =
            "INSERT INTO Director (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR =
            "UPDATE Director SET name = ? WHERE director_id = ?";
    private static final String FIND_DIRECTOR_BY_ID =
            "SELECT director_id, name FROM Director WHERE director_id = ?";
    private static final String FIND_ALL_DIRECTORS =
            "SELECT director_id, name FROM Director";
    private static final String COUNT_DIRECTOR_BY_ID =
            "SELECT COUNT(*) FROM Director WHERE director_id = ?";
    private static final String DELETE_DIRECTOR_BY_ID    =
            "DELETE FROM Director WHERE director_id = ?";

    @Override
    public Director addDirector(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_DIRECTOR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Не удалось сохранить режиссера: не получен id");
        }
        director.setId(generatedId.longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director oldDirector, Director newDirector) {
        Long directorId = oldDirector.getId();
        jdbcTemplate.update(UPDATE_DIRECTOR,
                newDirector.getName(),
                directorId);
        newDirector.setId(directorId);
        return newDirector;
    }

    @Override
    public Director getById(Long id) {
        return jdbcTemplate.queryForObject(FIND_DIRECTOR_BY_ID, directorRowMapper, id);
    }

    @Override
    public Map<Long, Director> getDirectors() {
        List<Director> directors = jdbcTemplate.query(FIND_ALL_DIRECTORS, directorRowMapper);
        if (directors.isEmpty()) return Collections.emptyMap();

        return directors.stream().collect(Collectors.toMap(Director::getId, d -> d));
    }

    @Override
    public void deleteDirector(long id) {
        jdbcTemplate.update(DELETE_DIRECTOR_BY_ID, id);
    }

    @Override
    public boolean existById(long id) {
        Integer count = jdbcTemplate.queryForObject(COUNT_DIRECTOR_BY_ID, Integer.class, id);
        return count > 0;
    }
}
