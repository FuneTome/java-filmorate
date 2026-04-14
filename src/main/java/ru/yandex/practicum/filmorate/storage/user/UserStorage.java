package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User oldUser, User newUser);
}
