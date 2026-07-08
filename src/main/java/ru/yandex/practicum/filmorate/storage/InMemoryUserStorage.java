package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User create(User user) {
        log.debug("Создание нового пользователя: login={}, email={}", user.getLogin(), user.getEmail());
        user.setId(currentId++);
        users.put(user.getId(), user);

        log.info("Пользователь создан: id={}, login={}, email={}",
                user.getId(), user.getLogin(), user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        log.debug("Обновление пользователя: id={}, login={}", user.getId(), user.getLogin());
        validateUserExists(user.getId());
        users.put(user.getId(), user);

        log.info("Пользователь обновлен: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        log.debug("Добавление друга: userId={}, friendId={}", id, friendId);
        validateUsersExist(id, friendId);

        User user = users.get(id);
        User friend = users.get(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        log.info("Дружба добавлена: userId={}, friendId={}", id, friendId);
        log.debug("У пользователя {} теперь {} друзей", id, user.getFriends().size());
        log.debug("У пользователя {} теперь {} друзей", friendId, friend.getFriends().size());
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        log.debug("Удаление друга: userId={}, friendId={}", id, friendId);
        validateUsersExist(id, friendId);

        User user = users.get(id);
        User friend = users.get(friendId);

        user.getFriends().removeIf(f -> f.equals(friendId));
        friend.getFriends().removeIf(f -> f.equals(id));

        log.info("Дружба удалена: userId={}, friendId={}", id, friendId);
        log.debug("У пользователя {} теперь {} друзей", id, user.getFriends().size());
        log.debug("У пользователя {} теперь {} друзей", friendId, friend.getFriends().size());
    }

    @Override
    public User getUser(Long id) {
        log.trace("Поиск пользователя по id: {}", id);
        validateUserExists(id);
        User user = users.get(id);

        log.debug("Найден пользователь: id={}, login={}", id, user.getLogin());
        return user;
    }

    @Override
    public List<User> getAll() {
        log.debug("Получение всех пользователей");
        List<User> allUsers = new ArrayList<>(users.values());

        log.debug("Найдено пользователей: {}", allUsers.size());
        return allUsers;
    }

    @Override
    public List<User> getAllFriends(Long id) {
        log.debug("Получение списка друзей пользователя: id={}", id);
        validateUserExists(id);

        List<User> friends = users.get(id).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());

        log.debug("Найдено друзей у пользователя {}: {}", id, friends.size());
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Поиск общих друзей: userId={}, otherId={}", id, otherId);
        validateUsersExist(id, otherId);

        List<User> commonFriends = users.get(id).getFriends()
                .stream()
                .filter(users.get(otherId).getFriends()::contains)
                .map(users::get)
                .collect(Collectors.toList());

        log.debug("Найдено общих друзей у пользователей {} и {}: {}", id, otherId, commonFriends.size());
        return commonFriends;
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