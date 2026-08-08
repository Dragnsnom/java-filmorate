package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    void addFriend(Long id, Long friendId);

    void deleteFriend(Long id, Long friendId);

    User getUser(Long id);

    List<User> getAll();

    List<User> getAllFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);

    void deleteUser(Long id);
}
