package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.EventType.FRIEND;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    public UserService(@org.springframework.beans.factory.annotation.Qualifier("userDbStorage") UserStorage userStorage,
                       EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUser(Long id) {
        return userStorage.getUser(id);
    }

    public void addFriend(Long id, Long friendId) {
         userStorage.addFriend(id, friendId);
         eventStorage.addEvent(id, FRIEND, ADD, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
        eventStorage.addEvent(id, FRIEND, REMOVE, friendId);
    }

    public List<User> getAllFriends(Long id) {
        return userStorage.getAllFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public List<Event> getFeed(Long id) {
        userStorage.getUser(id);
        return eventStorage.getEventsByUserId(id);
    }
}
