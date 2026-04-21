package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers().values();
    }

    public User getUser(Long id) {
        if (userStorage.findById(id)) {
            return userStorage.getById(id);
        }
        throw new NotFoundException("Юзер с id = " + id + " не найден");
    }

    public User addUser(User user) {
        isValid(user);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!userStorage.findById(newUser.getId())) {
            throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
        }
        isValid(newUser);
        User oldUser = userStorage.getById(newUser.getId());
        return userStorage.updateUser(oldUser, newUser);
    }

    public User addFriend(Long id, Long friendId) {
        if (!userStorage.findById(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(friendId)) {
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        }
        if (userStorage.getById(id).addFriend(friendId)) {
            userStorage.getById(friendId).addFriend(id);
            return userStorage.getById(id);
        }
        throw new NotFoundException("Такой человек уже есть в списке друзей");
    }

    public Collection<User> getFriends(Long id) {
        if (userStorage.findById(id)) {
            Set<Long> friendIds = userStorage.getById(id).getFriends();
            return friendIds.stream()
                    .map(userStorage.getUsers()::get)
                    .collect(Collectors.toList());
        }
        throw new NotFoundException("Юзер с id = " + id + " не найден");
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        if (!userStorage.findById(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(otherId)) {
            throw new NotFoundException("Юзер с id = " + otherId + " не найден");
        }
        Set<Long> friendsOfId = userStorage.getById(id).getFriends();
        Set<Long> friendsOfOtherId = userStorage.getById(otherId).getFriends();

        if (friendsOfId == null || friendsOfOtherId == null) {
            return null;
        }
        Set<Long> intersection = friendsOfId.stream()
                .filter(friendsOfOtherId::contains)
                .collect(Collectors.toSet());

        return intersection.stream()
                .map(userStorage.getUsers()::get)
                .collect(Collectors.toList());
    }

    public void deleteFriend(Long id, Long friendId) {
        if (!userStorage.findById(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        }
        if (!userStorage.findById(friendId)) {
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        }
        userStorage.getById(id).removeFriend(friendId);
        userStorage.getById(friendId).removeFriend(id);
    }

    private void isValid(User user) {
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}