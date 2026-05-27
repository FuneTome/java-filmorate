package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.SortByOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film oldFilm, Film newFilm);

    boolean findById(Long id);

    Film getById(Long id);

    Map<Long, Film> getFilms();

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getFilmsByDirector(long directorId, SortByOption sortBy);

    List<Film> searchFilms(String query, Set<SearchBy> by);
}