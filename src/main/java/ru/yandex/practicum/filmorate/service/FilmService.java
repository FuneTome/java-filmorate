package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms().values();
    }

    public Film addFilm(Film film) {
        isValid(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.getFilms().containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        isValid(newFilm);
        Film oldFilm = filmStorage.getById(newFilm.getId());
        return filmStorage.updateFilm(oldFilm, newFilm);
    }

    public Collection<Film> getListFilm(int count) {
        List<Film> sortedFilms = filmStorage.getFilms().values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
        return sortedFilms;
    }

    public Film addLike(Long id, Long userId) {
        if (!filmStorage.findById(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        if (!userStorage.findById(userId)) {
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!filmStorage.getById(id).addLike(userId)) {
            throw new NotFoundException("Такой человек уже ставил лайк");
        }
        return filmStorage.getFilms().get(id);
    }

    public void deleteLike(Long id, Long userId) {
        if (!filmStorage.findById(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        if (!userStorage.findById(userId)) {
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!filmStorage.getById(id).removeLike(userId)) {
            throw new NotFoundException("Такой человек не ставил лайк");
        }
    }

    public void isValid(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            throw new ValidationException("Дата релиза должна быть после 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Длительность должна быть положительной");
        }
    }
}