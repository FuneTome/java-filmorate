package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final EventStorage eventStorage;
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreService genreService,
                       MpaService mpaService,
                       EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.eventStorage = eventStorage;
    }

    public Collection<FilmDto> getFilms() {
        return filmStorage.getFilms().values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmRequest request) {
        Film film = toFilm(request);
        isValid(film);
        validateMpaAndGenres(film);
        Film saved = filmStorage.addFilm(film);
        return toDto(saved);
    }

    public FilmDto updateFilm(FilmRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.findById(request.getId())) {
            throw new NotFoundException("Фильм с id = " + request.getId() + " не найден");
        }
        Film film = toFilm(request);
        isValid(film);
        validateMpaAndGenres(film);
        Film oldFilm = filmStorage.getById(request.getId());
        Film updated = filmStorage.updateFilm(oldFilm, film);
        return toDto(updated);
    }

    public Collection<FilmDto> getListFilm(int count) {
        List<Film> popular = filmStorage.getPopularFilms(count);
        return popular.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto addLike(Long id, Long userId) {
        if (!filmStorage.findById(id)) throw new NotFoundException("Фильм с id = " + id + " не найден");
        if (!userStorage.findById(userId)) throw new NotFoundException("Юзер с id = " + userId + " не найден");
        if (!filmStorage.addLike(id, userId)) throw new NotFoundException("Такой человек уже ставил лайк");
        Film film = filmStorage.getById(id);
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.ADD)
                .entityId(id)
                .build();

        eventStorage.createEvent(event);
        return toDto(film);
    }

    public void deleteLike(Long id, Long userId) {
        if (!filmStorage.findById(id)) throw new NotFoundException("Фильм с id = " + id + " не найден");
        if (!userStorage.findById(userId)) throw new NotFoundException("Юзер с id = " + userId + " не найден");
        if (!filmStorage.removeLike(id, userId)) throw new NotFoundException("Такой человек не ставил лайк");
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.REMOVE)
                .entityId(id)
                .build();

        eventStorage.createEvent(event);
    }

    public FilmDto getFilmById(long filmId) {
        Film film = filmStorage.getById(filmId);
        return toDto(film);
    }

    private Film toFilm(FilmRequest request) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null) {
            Rating rating = new Rating();
            rating.setId(request.getMpa().getId());
            film.setRating(rating);
        }
        if (request.getGenres() != null) {
            Set<Genre> genres = request.getGenres().stream()
                    .map(g -> {
                        Genre genre = new Genre();
                        genre.setId(g.getId());
                        return genre;
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }
        return film;
    }

    private FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getRating() != null) {
            MpaDto mpaDto = mpaService.getRatingById(film.getRating().getId());
            dto.setMpa(mpaDto);
        }

        List<GenreDto> genreDtos = film.getGenres().stream()
                .map(genre -> genreService.getGenreById(genre.getId()))
                .collect(Collectors.toList());
        dto.setGenres(genreDtos);

        return dto;
    }

    public void isValid(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            throw new ValidationException("Дата релиза должна быть после 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Длительность должна быть положительной");
        }
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getRating() != null) {
            mpaService.getRatingById(film.getRating().getId());
        }
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                genreService.getGenreById(g.getId());
            }
        }
    }
}