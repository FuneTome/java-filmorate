package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final NamedParameterJdbcTemplate namedJdbc;

    private static final String INSERT_FILM =
            "INSERT INTO Film (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE Film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String INSERT_FILM_GENRE =
            "INSERT INTO Film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_FILM_DIRECTOR =
            "INSERT INTO Director_film (film_id, director_id) VALUES (?, ?)";
    private static final String FIND_FILM_BY_ID =
            "SELECT film_id, name, description, release_date, duration, rating_id FROM Film WHERE film_id = ?";
    private static final String FIND_ALL_FILMS =
            "SELECT film_id, name, description, release_date, duration, rating_id FROM Film";
    private static final String COUNT_FILM_BY_ID =
            "SELECT COUNT(*) FROM Film WHERE film_id = ?";
    private static final String FIND_GENRES_FOR_FILMS = """
            SELECT fg.film_id, g.genre_id, g.name FROM film_genre fg
            JOIN genre g ON fg.genre_id = g.genre_id
            ORDER BY g.genre_id
            """;
    private static final String INSERT_LIKE =
            "INSERT INTO Film_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE =
            "DELETE FROM Film_like WHERE film_id = ? AND user_id = ?";
    private static final String CHECK_LIKE_EXISTS =
            "SELECT COUNT(*) FROM Film_like WHERE film_id = ? AND user_id = ?";
    private static final String FIND_DIRECTORS_FOR_FILMS = """
            SELECT df.film_id,
                   d.director_id,
                   d.name
            FROM director_film df
            JOIN director d
                ON df.director_id = d.director_id
            """;
    private static final String FIND_FILM_BY_DIRECTOR_ORDER_BY_YEAR = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.rating_id
            FROM Film f
            JOIN Director_film df
                ON f.film_id = df.film_id
            WHERE df.director_id = ?
            ORDER BY f.release_date ASC, f.film_id ASC
            """;
    private static final String FIND_FILM_BY_DIRECTOR_ORDER_BY_LIKES = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.rating_id
            FROM Film f
            JOIN Director_film df
                ON f.film_id = df.film_id
            LEFT JOIN Film_like fl
                ON f.film_id = fl.film_id
            WHERE df.director_id = ?
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id
            ORDER BY COUNT(fl.user_id) DESC
            """;
    private static final String FIND_FILM_BY_PARAMS_BASE = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.rating_id
            FROM film f
            LEFT JOIN film_like fl
                ON f.film_id = fl.film_id
            LEFT JOIN director_film df
                ON f.film_id = df.film_id
            LEFT JOIN director d
                ON d.director_id = df.director_id
            """;
    private static final String DELETE_FILM = "DELETE FROM Film WHERE film_id = ?";

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRating().getId());
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Не удалось сохранить фильм: не получен id");
        }
        film.setId(generatedId.longValue());
        saveGenres(film);
        saveDirector(film);
        return film;
    }

    @Override
    public Film updateFilm(Film oldFilm, Film newFilm) {
        Long filmId = oldFilm.getId();
        newFilm.setId(filmId);
        jdbcTemplate.update(UPDATE_FILM,
                newFilm.getName(), newFilm.getDescription(), Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(), newFilm.getRating().getId(), filmId);
        jdbcTemplate.update("DELETE FROM Film_genre WHERE film_id = ?", filmId);
        newFilm.setId(filmId);
        saveGenres(newFilm);
        jdbcTemplate.update("DELETE FROM director_film WHERE film_id = ?", filmId);
        saveDirector(newFilm);
        return newFilm;
    }

    @Override
    public boolean findById(Long id) {
        Integer count = jdbcTemplate.queryForObject(COUNT_FILM_BY_ID, Integer.class, id);
        return count > 0;
    }

    @Override
    public Film getById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            enrichFilmWithDetails(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<Long, Film> getFilms() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, filmRowMapper);
        if (films.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Genre>> genresByFilm = loadAllGenres();
        Map<Long, Set<Director>> directorsByFilm = loadAllDirectors();
        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirector(directorsByFilm.getOrDefault(film.getId(), new HashSet<>()));
        });
        return films.stream().collect(Collectors.toMap(Film::getId, f -> f));
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.rating_id,
                       COUNT(fl.user_id) AS like_count
                FROM film f
                LEFT JOIN film_like fl
                    ON f.film_id = fl.film_id
                """);
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        if (year != null) {
            conditions.add("f.release_date >= ? AND f.release_date < ?");
            params.add(LocalDate.of(year, 1, 1));
            params.add(LocalDate.of(year + 1, 1, 1));
        }
        if (genreId != null) {
            conditions.add("f.film_id IN (SELECT film_id FROM film_genre WHERE genre_id = ?)");
            params.add(genreId);
        }
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }
        sql.append("GROUP BY f.film_id ORDER BY like_count DESC, f.film_id ASC LIMIT ?");
        params.add(count);
        List<Film> films = jdbcTemplate.query(sql.toString(), filmRowMapper, params.toArray());
        Map<Long, Set<Genre>> genresByFilm = loadAllGenres();
        Map<Long, Set<Director>> directorsByFilm = loadAllDirectors();
        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirector(directorsByFilm.getOrDefault(film.getId(), new HashSet<>()));
        });
        return films;
    }

    @Override
    public List<Film> getFilmsByDirector(long directorId, SortByOption sortBy) {
        List<Film> films;
        if (sortBy == SortByOption.YEAR) {
            films = jdbcTemplate.query(FIND_FILM_BY_DIRECTOR_ORDER_BY_YEAR, filmRowMapper, directorId);
        } else {
            films = jdbcTemplate.query(FIND_FILM_BY_DIRECTOR_ORDER_BY_LIKES, filmRowMapper, directorId);
        }
        Map<Long, Set<Genre>> genresByFilm = loadAllGenres();
        Map<Long, Set<Director>> directorsByFilm = loadAllDirectors();
        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirector(directorsByFilm.getOrDefault(film.getId(), new HashSet<>()));
        });
        return films;
    }

    @Override
    public List<Film> searchFilms(String query, Set<SearchBy> by) {
        StringBuilder sql = new StringBuilder(FIND_FILM_BY_PARAMS_BASE);
        List<String> conditions = new ArrayList<>();

        if (by.contains(SearchBy.TITLE)) {
            conditions.add("LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))");
        }

        if (by.contains(SearchBy.DIRECTOR)) {
            conditions.add("LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))");
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" OR ", conditions));
        }

        sql.append("""
                GROUP BY f.film_id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         f.rating_id
                ORDER BY COUNT(DISTINCT fl.user_id) DESC
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("query", query);

        List<Film> films = namedJdbc.query(sql.toString(), params, filmRowMapper);

        Map<Long, Set<Genre>> genresByFilm = loadAllGenres();
        Map<Long, Set<Director>> directorsByFilm = loadAllDirectors();
        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirector(directorsByFilm.getOrDefault(film.getId(), new HashSet<>()));
        });

        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT f.*
                FROM film f
                JOIN film_like l1
                    ON f.film_id = l1.film_id AND l1.user_id = ?
                JOIN film_like l2
                    ON f.film_id = l2.film_id AND l2.user_id = ?
                LEFT JOIN film_like all_likes
                    ON f.film_id = all_likes.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(all_likes.user_id) DESC
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, userId, friendId);
        films.forEach(this::enrichFilmWithDetails);

        return films;
    }

    @Override
    public void deleteFilm(Long id) {
        jdbcTemplate.update(DELETE_FILM, id);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
        }
    }

    private void saveDirector(Film film) {
        if (film.getDirector() == null || film.getDirector().isEmpty()) {
            return;
        }
        for (Director director : film.getDirector()) {
            jdbcTemplate.update(INSERT_FILM_DIRECTOR, film.getId(), director.getId());
        }
    }

    private void enrichFilmWithDetails(Film film) {

        if (film == null) {
            return;
        }

        List<Genre> genres = jdbcTemplate.query("""
                        SELECT g.genre_id,
                               g.name
                        FROM film_genre fg
                        JOIN genre g
                            ON fg.genre_id = g.genre_id
                        WHERE fg.film_id = ?
                        ORDER BY g.genre_id
                        """,
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new HashSet<>(genres));
        List<Director> directors = jdbcTemplate.query("""
                        SELECT d.director_id,
                               d.name
                        FROM director_film df
                        JOIN director d
                            ON df.director_id = d.director_id
                        WHERE df.film_id = ?
                        """,
                (rs, rowNum) -> new Director(rs.getLong("director_id"), rs.getString("name")), film.getId());
        film.setDirector(new HashSet<>(directors));
    }

    private Map<Long, Set<Genre>> loadAllGenres() {
        List<Map<String, Object>> rows = jdbcTemplate.query(FIND_GENRES_FOR_FILMS,
                (rs, rowNum) -> Map.of("filmId", rs.getLong("film_id"), "genreId", rs.getInt("genre_id"), "genreName", rs.getString("name")));
        Map<Long, Set<Genre>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = ((Number) row.get("filmId")).longValue();
            Genre genre = new Genre((int) row.get("genreId"), (String) row.get("genreName"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }
        return result;
    }

    private Map<Long, Set<Director>> loadAllDirectors() {
        List<Map<String, Object>> rows = jdbcTemplate.query(FIND_DIRECTORS_FOR_FILMS,
                (rs, rowNum) -> Map.of("filmId", rs.getLong("film_id"), "directorId", rs.getInt("director_id"), "directorName", rs.getString("name")));
        Map<Long, Set<Director>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = ((Number) row.get("filmId")).longValue();
            Long directorId = ((Number) row.get("directorId")).longValue();
            Director director = new Director(directorId, (String) row.get("directorName"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        }
        return result;
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS, Integer.class, filmId, userId);
        if (count > 0) {
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