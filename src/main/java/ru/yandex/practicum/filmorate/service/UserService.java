package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public Collection<User> getUsers() {
        log.info("Запрос на получение всех пользователей");
        return userStorage.getUsers().values();
    }

    public User getUser(Long id) {
        log.info("Запрос на получение пользователя по id: {}", id);
        if (userStorage.findById(id)) {
            return userStorage.getById(id);
        }
        log.warn("Пользователь с id {} не найден", id);
        throw new NotFoundException("Юзер с id = " + id + " не найден");
    }

    public User addUser(User user) {
        log.info("Запрос на добавление нового пользователя: {}", user.getLogin());
        isValid(user);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        User saved = userStorage.addUser(user);
        log.info("Пользователь успешно добавлен с id: {}", saved.getId());
        return saved;
    }

    public User updateUser(User newUser) {
        log.info("Запрос на обновление пользователя с id: {}", newUser.getId());
        if (newUser.getId() == null) {
            log.warn("Попытка обновления пользователя без указания id");
            throw new ValidationException("Id должен быть указан");
        }
        if (!userStorage.findById(newUser.getId())) {
            log.warn("Пользователь с id {} не найден для обновления", newUser.getId());
            throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
        }
        isValid(newUser);
        User oldUser = userStorage.getById(newUser.getId());
        User updated = userStorage.updateUser(oldUser, newUser);
        log.info("Пользователь с id {} успешно обновлён", updated.getId());
        return updated;
    }

    public User addFriend(Long id, Long friendId) {
        log.info("Запрос на добавление друга id: {} пользователю id: {}", friendId, id);
        if (!userStorage.findById(id)) {
            log.warn("Пользователь id: {} не найден", id);
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(friendId)) {
            log.warn("Друг id: {} не найден", friendId);
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        }
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
        return userStorage.getById(id);
    }

    public Collection<User> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя id: {}", id);
        if (!userStorage.findById(id)) {
            log.warn("Пользователь id: {} не найден", id);
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.info("Запрос на получение общих друзей пользователей id: {} и id: {}", id, otherId);
        if (!userStorage.findById(id)) {
            log.warn("Пользователь id: {} не найден", id);
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(otherId)) {
            log.warn("Пользователь id: {} не найден", otherId);
            throw new NotFoundException("Юзер с id = " + otherId + " не найден");
        }
        return userStorage.getCommonFriends(id, otherId);
    }

    public void deleteFriend(Long id, Long friendId) {
        log.info("Запрос на удаление друга id: {} у пользователя id: {}", friendId, id);
        if (!userStorage.findById(id)) {
            log.warn("Пользователь id: {} не найден", id);
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(friendId)) {
            log.warn("Друг id: {} не найден", friendId);
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        }
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

    private void isValid(User user) {
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
            log.warn("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}