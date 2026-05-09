package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
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
            if (user != null) {
                enrichUserWithFriends(user);
            }
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public Map<Long, User> getUsers() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS, userRowMapper);
        Map<Long, Set<Long>> allFriends = loadAllFriends();
        users.forEach(u -> u.setFriends(allFriends.getOrDefault(u.getId(), new HashSet<>())));
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

    private void enrichUserWithFriends(User user) {
        List<Long> friendIds = jdbcTemplate.query(
                "SELECT friend_id FROM Friendship WHERE user_id = ? AND friendship_status_id = 2",
                (rs, rowNum) -> rs.getLong("friend_id"),
                user.getId()
        );
        user.setFriends(new HashSet<>(friendIds));
    }

    private Map<Long, Set<Long>> loadAllFriends() {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT user_id, friend_id FROM Friendship WHERE friendship_status_id = 2",
                (rs, rowNum) -> Map.of(
                        "userId", rs.getLong("user_id"),
                        "friendId", rs.getLong("friend_id")
                )
        );
        Map<Long, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = (Long) row.get("userId");
            Long friendId = (Long) row.get("friendId");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        }
        return result;
    }
}