package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private InMemoryUserStorage imus;

    public UserService(InMemoryUserStorage imus) {
        this.imus = imus;
    }

    public Collection<User> getUsers() {
        return imus.getUsers().values();
    }

    public User getUser(Long id) {
        if (isValidId(id)) {
            return imus.getUsers().get(id);
        }
        throw new NotFoundException("Юзер с id = " + id + " не найден");
    }

    public User addUser(User user) {
        if (!isValid(user)) {
            log.warn("Валидация пользователя не пройдена");
            throw new ValidationException("Валидация пользователя не пройдена");
        }
        return imus.addUser(user);
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!imus.getUsers().containsKey(newUser.getId())) {
            throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
        } else {
            if (isValid(newUser)) {
                User oldUser = imus.getUsers().get(newUser.getId());
                return imus.updateUser(oldUser, newUser);
            } else {
                log.warn("Валидация пользователя при обновлении не пройдена");
                return null;
            }
        }
    }

    public User addFriend(Long id, Long friendId) {
        if (!isValidId(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        } else if (!isValidId(friendId)) {
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        } else {
            if (imus.getUsers().get(id).addFriend(friendId)) {
                imus.getUsers().get(friendId).addFriend(id);
            } else {
                throw new NotFoundException("Такой человек уже есть в списке друзей");
            }
            return imus.getUsers().get(id);
        }
    }

    public Collection<User> getFriends(Long id) {
        if (isValidId(id)) {
            Set<Long> friendIds = imus.getUsers().get(id).getFriends();
            return friendIds.stream()
                    .map(imus.getUsers()::get)
                    .collect(Collectors.toList());
        }
        throw new NotFoundException("Юзер с id = " + id + " не найден");
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        if (!isValidId(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        } else if (!isValidId(otherId)) {
            throw new NotFoundException("Юзер с id = " + otherId + " не найден");
        } else {
            Set<Long> intersection = imus.getUsers().get(id).getFriends().stream()
                    .filter(imus.getUsers().get(otherId).getFriends()::contains)
                    .collect(Collectors.toSet());
            return intersection.stream()
                    .map(imus.getUsers()::get)
                    .collect(Collectors.toList());
        }
    }

    public void deleteFriend(Long id, Long friendId) {
        if (!isValidId(id)) {
            throw new NotFoundException("Юзер с id = " + id + " не найден");
        } else if (!isValidId(friendId)) {
            throw new NotFoundException("Юзер с id = " + friendId + " не найден");
        } else {
            imus.getUsers().get(id).removeFriend(friendId);
            imus.getUsers().get(friendId).removeFriend(id);
        }
    }

    private boolean isValid(User user) {
        try {
            if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            } else {
                return true;
            }
        } catch (ValidationException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidId(Long id) {
        if (imus.getUsers().containsKey(id)) {
            return true;
        }
        return false;
    }
}