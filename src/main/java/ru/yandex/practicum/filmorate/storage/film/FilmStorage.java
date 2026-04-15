package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    Film addFilm(Film film);
    Film updateFilm(Film oldFilm, Film newFilm);
    boolean findById(Long id);
    Film getById(Long id);
    Map<Long, Film> getFilms();
}
