package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long id = 1L;

    @Override
    public Film updateFilm(Film oldFilm, Film newFilm) {
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        films.put(oldFilm.getId(), oldFilm);
        log.info("Фильм обновлен: {}", newFilm.getId());
        return oldFilm;
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(id);
        films.put(id++, film);
        log.info("Фильм добавлен: {}", film.getId());
        return film;
    }

    @Override
    public boolean findById(Long id) {
        return films.containsKey(id);
    }

    @Override
    public Film getById(Long id) {
        return films.get(id);
    }
}
