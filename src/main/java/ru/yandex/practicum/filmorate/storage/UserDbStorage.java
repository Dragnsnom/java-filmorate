package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        log.debug("Создание нового пользователя в БД: login={}, email={}", user.getLogin(), user.getEmail());
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        log.info("Пользователь создан в БД: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        log.debug("Обновление пользователя в БД: id={}, login={}", user.getId(), user.getLogin());
        getUser(user.getId()); // Бросит исключение, если не найден

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        log.info("Пользователь обновлен в БД: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        log.debug("Добавление друга в БД: userId={}, friendId={}", id, friendId);
        getUser(id);
        getUser(friendId);

        String sql = "MERGE INTO friends (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, friendId);
        log.info("Друг добавлен в БД: userId={}, friendId={}", id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        log.debug("Удаление друга из БД: userId={}, friendId={}", id, friendId);
        getUser(id);
        getUser(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, id, friendId);
        log.info("Друг удален из БД: userId={}, friendId={}", id, friendId);
    }

    @Override
    public User getUser(Long id) {
        log.trace("Поиск пользователя в БД по id: {}", id);
        return findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    @Override
    public List<User> getAll() {
        log.debug("Получение всех пользователей из БД");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        for (User user : users) {
            user.setFriends(new HashSet<>(getFriendIds(user.getId())));
        }
        return users;
    }

    @Override
    public List<User> getAllFriends(Long id) {
        log.debug("Получение списка друзей из БД для пользователя: id={}", id);
        getUser(id);

        String sql = "SELECT u.* FROM users u INNER JOIN friends f ON u.id = f.friend_id WHERE f.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, this::mapRowToUser, id);
        for (User friend : friends) {
            friend.setFriends(new HashSet<>(getFriendIds(friend.getId())));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Поиск общих друзей из БД: userId={}, otherId={}", id, otherId);
        getUser(id);
        getUser(otherId);

        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN friends f1 ON u.id = f1.friend_id " +
                "INNER JOIN friends f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        List<User> common = jdbcTemplate.query(sql, this::mapRowToUser, id, otherId);
        for (User c : common) {
            c.setFriends(new HashSet<>(getFriendIds(c.getId())));
        }
        return common;
    }

    public Optional<User> findUserById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        user.setFriends(new HashSet<>(getFriendIds(id)));
        return Optional.of(user);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private List<Long> getFriendIds(long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }
}
