package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String INSERT_FILM =
            "INSERT INTO Film (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE Film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String DELETE_FILM_GENRES =
            "DELETE FROM Film_genre WHERE film_id = ?";
    private static final String INSERT_FILM_GENRE =
            "INSERT INTO Film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_FILM_BY_ID =
            "SELECT film_id, name, description, release_date, duration, rating_id FROM Film WHERE film_id = ?";
    private static final String FIND_ALL_FILMS =
            "SELECT film_id, name, description, release_date, duration, rating_id FROM Film";
    private static final String COUNT_FILM_BY_ID =
            "SELECT COUNT(*) FROM Film WHERE film_id = ?";
    private static final String FIND_GENRES_FOR_FILMS =
            "SELECT fg.film_id, fg.genre_id FROM Film_genre fg";
    private static final String FIND_LIKES_FOR_FILMS =
            "SELECT film_id, user_id FROM Film_like";
    private static final String INSERT_LIKE =
            "INSERT INTO Film_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE =
            "DELETE FROM Film_like WHERE film_id = ? AND user_id = ?";
    private static final String CHECK_LIKE_EXISTS =
            "SELECT COUNT(*) FROM Film_like WHERE film_id = ? AND user_id = ?";

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRating());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Не удалось сохранить фильм: не получен id");
        }
        film.setId(generatedId.longValue());

        saveGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film oldFilm, Film newFilm) {
        Long filmId = oldFilm.getId();
        jdbcTemplate.update(UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getRating(),
                filmId);

        jdbcTemplate.update(DELETE_FILM_GENRES, filmId);
        newFilm.setId(filmId);
        saveGenres(newFilm);
        return newFilm;
    }

    @Override
    public boolean findById(Long id) {
        Integer count = jdbcTemplate.queryForObject(COUNT_FILM_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Film getById(Long id) {
        Film film = jdbcTemplate.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
        if (film != null) {
            enrichFilmWithGenresAndLikes(film);
        }
        return film;
    }

    @Override
    public Map<Long, Film> getFilms() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, filmRowMapper);
        if (films.isEmpty()) return Collections.emptyMap();

        Map<Long, Set<Long>> genresByFilm = loadAllGenres();
        Map<Long, Set<Long>> likesByFilm = loadAllLikes();

        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
        });

        return films.stream().collect(Collectors.toMap(Film::getId, f -> f));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        for (Long genreId : film.getGenres()) {
            jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genreId);
        }
    }

    private void enrichFilmWithGenresAndLikes(Film film) {
        List<Long> genreIds = jdbcTemplate.query(
                "SELECT genre_id FROM Film_genre WHERE film_id = ?",
                (rs, rowNum) -> rs.getLong("genre_id"),
                film.getId()
        );
        film.setGenres(new HashSet<>(genreIds));

        List<Long> likes = jdbcTemplate.query(
                "SELECT user_id FROM Film_like WHERE film_id = ?",
                (rs, rowNum) -> rs.getLong("user_id"),
                film.getId()
        );
        film.setLikes(new HashSet<>(likes));
    }

    private Map<Long, Set<Long>> loadAllGenres() {
        List<Map<String, Object>> rows = jdbcTemplate.query(FIND_GENRES_FOR_FILMS,
                (rs, rowNum) -> Map.of(
                        "filmId", rs.getLong("film_id"),
                        "genreId", rs.getLong("genre_id")
                ));
        Map<Long, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = (Long) row.get("filmId");
            Long genreId = (Long) row.get("genreId");
            result.computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(genreId);
        }
        return result;
    }

    private Map<Long, Set<Long>> loadAllLikes() {
        List<Map<String, Object>> rows = jdbcTemplate.query(FIND_LIKES_FOR_FILMS,
                (rs, rowNum) -> Map.of(
                        "filmId", rs.getLong("film_id"),
                        "userId", rs.getLong("user_id")
                ));
        Map<Long, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = (Long) row.get("filmId");
            Long userId = (Long) row.get("userId");
            result.computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(userId);
        }
        return result;
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS, Integer.class, filmId, userId);
        if (count != null && count > 0) {
            return false;
        }
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
        return true;
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        int rowsAffected = jdbcTemplate.update(DELETE_LIKE, filmId, userId);
        return rowsAffected > 0;
    }
}