package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film oldFilm, Film newFilm);

    boolean findById(Long id);

    Film getById(Long id);

    Map<Long, Film> getFilms();

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count);

    List<Film> getFilmsByDirector(long directorId, String sortBy);
}