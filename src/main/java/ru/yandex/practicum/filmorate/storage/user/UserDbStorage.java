package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    private static final String INSERT_USER =
            "INSERT INTO Users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE Users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String FIND_USER_BY_ID =
            "SELECT user_id, email, login, name, birthday FROM Users WHERE user_id = ?";
    private static final String FIND_ALL_USERS =
            "SELECT user_id, email, login, name, birthday FROM Users";
    private static final String COUNT_USER_BY_ID =
            "SELECT COUNT(*) FROM Users WHERE user_id = ?";
    private static final String INSERT_FRIEND =
            "INSERT INTO Friendship (user_id, friend_id, friendship_status_id) VALUES (?, ?, 2)";
    private static final String DELETE_FRIEND =
            "DELETE FROM Friendship WHERE user_id = ? AND friend_id = ?";
    private static final String CHECK_FRIEND_EXISTS =
            "SELECT COUNT(*) FROM Friendship WHERE user_id = ? AND friend_id = ?";
    private static final String GET_RECOMMENDATION =
            "SELECT f.* FROM film f JOIN film_like fl ON f.film_id = fl.film_id " +
            "WHERE fl.user_id = (SELECT user_id FROM film_like WHERE film_id IN " +
            "(SELECT film_id FROM film_like WHERE user_id = ?)" +
            "AND user_id != ?" +
            "GROUP BY user_id ORDER BY COUNT(*) DESC LIMIT 1)" +
            "AND f.film_id NOT IN (SELECT film_id FROM film_like WHERE user_id = ?);";
    private final FilmRowMapper filmRowMapper;

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Не удалось сохранить пользователя: не получен id");
        }
        user.setId(generatedId.longValue());
        return user;
    }

    @Override
    public User updateUser(User oldUser, User newUser) {
        Long userId = oldUser.getId();
        jdbcTemplate.update(UPDATE_USER,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                userId);
        newUser.setId(userId);
        return newUser;
    }

    @Override
    public boolean findById(Long id) {
        Integer count = jdbcTemplate.queryForObject(COUNT_USER_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public User getById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER_BY_ID, userRowMapper, id);
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public List<User> getFriends(long userId) {
        return jdbcTemplate.query(
                "SELECT u.* FROM users u JOIN friendship f ON u.user_id = f.friend_id WHERE f.user_id = ? AND f.friendship_status_id = 2",
                userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        return jdbcTemplate.query(
                "SELECT u.* FROM users u " +
                        "JOIN friendship f1 ON u.user_id = f1.friend_id AND f1.user_id = ? AND f1.friendship_status_id = 2 " +
                        "JOIN friendship f2 ON u.user_id = f2.friend_id AND f2.user_id = ? AND f2.friendship_status_id = 2",
                userRowMapper, userId, otherId);
    }

    @Override
    public Map<Long, User> getUsers() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS, userRowMapper);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_FRIEND_EXISTS, Integer.class, userId, friendId);
        if (count != null && count > 0) {
            return false;
        }
        jdbcTemplate.update(INSERT_FRIEND, userId, friendId);
        return true;
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        int rowsAffected = jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
        return rowsAffected > 0;
    }

    @Override
    public List<Film> getRecommendations(Long id) {
        return jdbcTemplate.query(GET_RECOMMENDATION, filmRowMapper, id, id, id);
    }
}