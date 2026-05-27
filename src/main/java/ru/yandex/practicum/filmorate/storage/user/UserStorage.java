package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User oldUser, User newUser);

    boolean findById(Long id);

    User getById(Long id);

    Map<Long, User> getUsers();

    boolean addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);

    List<Film> getRecommendations(Long id);

    void deleteUser(Long id);
}