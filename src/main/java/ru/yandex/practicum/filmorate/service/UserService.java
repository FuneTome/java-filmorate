package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final DirectorService directorService;
    private final EventStorage eventStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       EventStorage eventStorage,
                       GenreService genreService,
                       MpaService mpaService,
                       DirectorService directorService) {
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.directorService = directorService;
        this.eventStorage = eventStorage;
    }

    public Collection<UserDto> getUsers() {
        log.info("Запрос на получение всех пользователей");
        return userStorage.getUsers().values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUser(Long id) {
        log.info("Запрос на получение пользователя по id: {}", id);
        if (!userStorage.findById(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        User user = userStorage.getById(id);
        return toDto(user);
    }

    public UserDto addUser(UserRequest request) {
        log.info("Запрос на добавление нового пользователя: {}", request.getLogin());
        User user = toUser(request);
        isValid(user);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        User saved = userStorage.addUser(user);
        log.info("Пользователь успешно добавлен с id: {}", saved.getId());
        return toDto(saved);
    }

    public UserDto updateUser(UserRequest request) {
        log.info("Запрос на обновление пользователя с id: {}", request.getId());
        if (request.getId() == null) {
            log.warn("Попытка обновления пользователя без указания id");
            throw new ValidationException("Id должен быть указан");
        }
        if (!userStorage.findById(request.getId())) {
            log.warn("Пользователь с id {} не найден для обновления", request.getId());
            throw new NotFoundException("Юзер с id = " + request.getId() + " не найден");
        }
        User user = toUser(request);
        user.setId(request.getId());
        isValid(user);
        User oldUser = userStorage.getById(request.getId());
        User updated = userStorage.updateUser(oldUser, user);
        log.info("Пользователь с id {} успешно обновлён", updated.getId());
        return toDto(updated);
    }

    public UserDto addFriend(Long id, Long friendId) {
        log.info("Запрос на добавление друга id: {} пользователю id: {}", friendId, id);
        checkUserExists(id);
        checkUserExists(friendId);
        if (!userStorage.addFriend(id, friendId)) {
            log.warn("Пользователь id: {} уже в друзьях у id: {}", friendId, id);
            throw new NotFoundException("Такой человек уже есть в списке друзей");
        }
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(id)
                .eventType(EventType.FRIEND)
                .operation(Operation.ADD)
                .entityId(friendId)
                .build();

        eventStorage.createEvent(event);
        log.info("Друг успешно добавлен");
        User user = userStorage.getById(id);
        return toDto(user);
    }

    public Collection<UserDto> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя id: {}", id);
        checkUserExists(id);
        return userStorage.getFriends(id).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.info("Запрос на получение общих друзей пользователей id: {} и id: {}", id, otherId);
        checkUserExists(id);
        checkUserExists(otherId);
        return userStorage.getCommonFriends(id, otherId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void deleteFriend(Long id, Long friendId) {
        log.info("Запрос на удаление друга id: {} у пользователя id: {}", friendId, id);
        checkUserExists(id);
        checkUserExists(friendId);
        userStorage.removeFriend(id, friendId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(id)
                .eventType(EventType.FRIEND)
                .operation(Operation.REMOVE)
                .entityId(friendId)
                .build();

        eventStorage.createEvent(event);
        log.info("Друг успешно удалён");
    }

    public Collection<Event> getUserFeed(Long id) {
        if (!userStorage.findById(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        return eventStorage.getUserFeed(id);
    }

    public Collection<FilmDto> getRecommendations(Long id) {
        log.info("Запрос на получение рекоммендации для пользователя с id: {}", id);
        checkUserExists(id);
        return userStorage.getRecommendations(id).stream()
                .map(this::toFilmDto)
                .collect(Collectors.toList());
    }

    private void checkUserExists(long userId) {
        if (!userStorage.findById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Юзер с id = " + userId + " не найден");
        }
    }

    private User toUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        return user;
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        return dto;
    }

    private FilmDto toFilmDto(Film film) {
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

    private void isValid(User user) {
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}