package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
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
    private final DirectorService directorService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreService genreService,
                       MpaService mpaService,
                       DirectorService directorService,
                       EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.directorService = directorService;
        this.eventStorage = eventStorage;
    }

    public Collection<FilmDto> getFilms() {
        log.info("Запрос на получение всех фильмов");
        return filmStorage.getFilms().values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmRequest request) {
        log.info("Запрос на добавление нового фильма: {}", request.getName());
        Film film = toFilm(request);
        isValid(film);
        validateMpaAndGenres(film);
        Film saved = filmStorage.addFilm(film);
        log.info("Фильм успешно добавлен с id: {}", saved.getId());
        return toDto(saved);
    }

    public FilmDto updateFilm(FilmRequest request) {
        log.info("Запрос на обновление фильма с id: {}", request.getId());
        if (request.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ValidationException("Id должен быть указан");
        }
        checkFilmExists(request.getId());
        Film film = toFilm(request);
        isValid(film);
        validateMpaAndGenres(film);
        Film oldFilm = filmStorage.getById(request.getId());
        filmStorage.updateFilm(oldFilm, film);
        Film updated = filmStorage.getById(request.getId());
        log.info("Фильм с id {} успешно обновлён", updated.getId());
        return toDto(updated);
    }

    public Collection<FilmDto> getListFilm(int count) {
        return getListFilm(count, null, null);
    }

    public Collection<FilmDto> getListFilm(int count, Integer genreId, Integer year) {
        log.info("Запрос на получение {} популярных фильмов (жанр: {}, год: {})", count, genreId, year);

        if (genreId != null) {
            genreService.getGenreById(genreId);
        }
        if (year != null) {
            if (year < FIRST_FILM_DATE.getYear()) {
                throw new ValidationException("Год не может быть раньше " + FIRST_FILM_DATE.getYear());
            }
            if (year > LocalDate.now().getYear()) {
                throw new ValidationException("Год не может быть в будущем");
            }
        }

        List<Film> popular = filmStorage.getPopularFilms(count, genreId, year);
        return popular.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto addLike(Long id, Long userId) {
        log.info("Запрос на добавление лайка фильму id: {} от пользователя id: {}", id, userId);
        checkFilmExists(id);
        checkUserExists(userId);
        if (!filmStorage.addLike(id, userId)) {
            log.warn("Пользователь id: {} уже ставил лайк фильму id: {}", userId, id);
            throw new NotFoundException("Такой человек уже ставил лайк");
        }
        Film film = filmStorage.getById(id);
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.ADD)
                .entityId(id)
                .build();
        eventStorage.createEvent(event);
        log.info("Лайк успешно добавлен");
        return toDto(film);
    }

    public void deleteLike(Long id, Long userId) {
        log.info("Запрос на удаление лайка фильму id: {} от пользователя id: {}", id, userId);
        checkFilmExists(id);
        checkUserExists(userId);
        if (!filmStorage.removeLike(id, userId)) {
            log.warn("Пользователь id: {} не ставил лайк фильму id: {}", userId, id);
            throw new NotFoundException("Такой человек не ставил лайк");
        }
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.REMOVE)
                .entityId(id)
                .build();
        eventStorage.createEvent(event);
        log.info("Лайк успешно удалён");
    }

    public FilmDto getFilmById(long filmId) {
        log.info("Запрос на получение фильма по id: {}", filmId);
        Film film = filmStorage.getById(filmId);
        return toDto(film);
    }

    public Collection<FilmDto> getFilmsByDirector(long directorId, SortByOption sortBy) {
        log.info("Запрос на получение фильмов режиссёра id: {} с сортировкой: {}", directorId, sortBy);
        checkDirectorExists(directorId);
        List<Film> films = filmStorage.getFilmsByDirector(directorId, sortBy);
        return films.stream().map(this::toDto).collect(Collectors.toList());
    }

    private void checkFilmExists(long id) {
        if (!filmStorage.findById(id)) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    private void checkUserExists(long userId) {
        if (!userStorage.findById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        }
    }

    private void checkDirectorExists(long directorId) {
        if (!directorService.existsById(directorId)) {
            log.warn("Режиссёр с id {} не найден", directorId);
            throw new NotFoundException("Режиссёр с id = " + directorId + " не найден");
        }
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

        if (request.getDirectors() != null) {
            Set<Director> directors = request.getDirectors().stream()
                    .map(g -> {
                        Director director = new Director();
                        director.setId(g.getId());
                        return director;
                    })
                    .collect(Collectors.toSet());
            film.setDirector(directors);
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
        List<DirectorDto> directorDtos = film.getDirector().stream()
                .map(director -> directorService.getDirectorById(director.getId()))
                .collect(Collectors.toList());
        dto.setDirectors(directorDtos);
        return dto;
    }

    public void isValid(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.warn("Дата релиза {} недопустима", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть после 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("Длительность фильма {} недопустима", film.getDuration());
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