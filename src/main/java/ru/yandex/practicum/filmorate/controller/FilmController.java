package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
    private Long id = 1L;

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (!isValid(film)) {
            log.warn("Валидация фильма не пройдена");
            throw new ValidationException("Валидация фильма не пройдена");
        }
        film.setId(id);
        films.put(id++, film);
        log.info("Фильм добавлен: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        } else {
            Film oldFilm = films.get(newFilm.getId());
            if (isValid(newFilm)) {
                oldFilm.setName(newFilm.getName());
                oldFilm.setDescription(newFilm.getDescription());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                oldFilm.setDuration(newFilm.getDuration());
                log.info("Фильм обновлен: {}", newFilm.getId());
                return oldFilm;
            } else {
                log.warn("Валидация фильма при обновлении не пройдена");
                throw new ValidationException("Валидация фильма при обновлении не пройдена");
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
}