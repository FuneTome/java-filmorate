package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final Logger logger = Logger.getLogger(FilmController.class.getName());

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (!isValid(film)) {
            logger.log(Level.WARNING, "Валидация фильма не пройдена");
            throw new ValidationException("Валидация фильма не пройдена");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        logger.log(Level.INFO, "Фильм добавлен: " + film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (isValid(newFilm)) {
                oldFilm.setName(newFilm.getName());
                oldFilm.setDescription(newFilm.getDescription());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                oldFilm.setDuration(newFilm.getDuration());
                logger.log(Level.INFO, "Фильм обновлен: " + newFilm.getId());
                return oldFilm;
            } else {
                logger.log(Level.WARNING, "Валидация фильма при обновлении не пройдена");
                throw new ValidationException("Валидация фильма при обновлении не пройдена");
            }
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public boolean isValid(Film film) {
        try {
            if (film.getName() == null || film.getName().isBlank()) {
                throw new ValidationException("Название не должно быть пустым!");
            } else if (film.getDescription().length() > 200) {
                throw new ValidationException("Максимальная длина описания - 200 символов");
            } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза должна быть после 28 декабря 1895 года");
            } else if (film.getDuration() < 0) {
                throw new ValidationException("Длительность должна быть положительной");
            } else {
                return true;
            }
        } catch (ValidationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
