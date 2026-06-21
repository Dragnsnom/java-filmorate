package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User create(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        validateUserExists(user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        validateUsersExist(id, friendId);

        User user = users.get(id);
        User friend = users.get(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        validateUsersExist(id, friendId);

        User user = users.get(id);
        User friend = users.get(friendId);

        user.getFriends().removeIf(f -> f.equals(friendId));
        friend.getFriends().removeIf(f -> f.equals(id));
    }

    @Override
    public User getUser(Long id) {
        validateUserExists(id);
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<User> getAllFriends(Long id) {
        validateUserExists(id);

        return users.get(id).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUsersExist(id, otherId);

        return users.get(id).getFriends()
                .stream()
                .filter(users.get(otherId).getFriends()::contains)
                .map(users::get)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    private void validateUsersExist(Long id, Long friendId) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (!users.containsKey(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
    }
}