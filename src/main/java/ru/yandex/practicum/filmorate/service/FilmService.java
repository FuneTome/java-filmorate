package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private InMemoryFilmStorage imfs;
    private InMemoryUserStorage imus;
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    public FilmService(InMemoryFilmStorage imfs, InMemoryUserStorage imus) {
        this.imfs = imfs;
        this.imus = imus;
    }

    public Collection<Film> getFilms() {
        return imfs.getFilms().values();
    }

    public Film addFilm(Film film) {
        if (!isValid(film)) {
            log.warn("Валидация фильма не пройдена");
            throw new ValidationException("Валидация фильма не пройдена");
        }
        return imfs.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!imfs.getFilms().containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        } else {
            if (isValid(newFilm)) {
                Film oldFilm = imfs.getFilms().get(newFilm.getId());
                return imfs.updateFilm(oldFilm, newFilm);
            } else {
                log.warn("Валидация фильма при обновлении не пройдена");
                throw new ValidationException("Валидация фильма при обновлении не пройдена");
            }
        }
    }

    public Collection<Film> getListFilm(int count) {
        List<Film> sortedFilms = imfs.getFilms().values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
        return sortedFilms;
    }

    public Film addLike(Long id, Long userId) {
        if(!isValidId(id)){
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        } else if(!imus.getUsers().containsKey(userId)) {
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        } else {
            if (!imfs.getFilms().get(id).addLike(userId)) {
                throw new NotFoundException("Такой человек уже ставил лайк");
            }
            return imfs.getFilms().get(id);
        }
    }

    public void deleteLike(Long id, Long userId) {
        if(!isValidId(id)){
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        } else if(!imus.getUsers().containsKey(userId)) {
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        } else {
            if(!imfs.getFilms().get(id).removeLike(userId)) {
                throw new NotFoundException("Такой человек не ставил лайк");
            }
        }
    }

    public boolean isValid(Film film) {
        try {
            if (film.getReleaseDate().isBefore(firstFilmDate)) {
                throw new ValidationException("Дата релиза должна быть после 28 декабря 1895 года");
            } else if (film.getDuration() < 0) {
                throw new ValidationException("Длительность должна быть положительной");
            } else {
                return true;
            }
        } catch (ValidationException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidId(Long id) {
        if (imfs.getFilms().containsKey(id)) {
            return true;
        }
        return false;
    }
}
