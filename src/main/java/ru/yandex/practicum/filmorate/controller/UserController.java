package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET /users - получение всех пользователей");
        List<User> users = userService.getAllUsers();
        log.debug("GET /users - найдено пользователей: {}", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User getUser(
            @PathVariable Long id
    ) {
        log.info("GET /users{id} - получение данных о пользователе");
        User user = userService.getUser(id);
        log.debug("GET /users{id} - найден пользователь {}", user.getId());
        return user;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("POST /users - создание пользователя с логином: {}", user.getLogin());
        User createdUser = userService.createUser(user);
        log.info("POST /users - пользователь создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("PUT /users - обновление пользователя: id={}", user.getId());
        User updatedUser = userService.updateUser(user);
        log.info("PUT /users - пользователь обновлен: id={}", updatedUser.getId());
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId) {

        userService.addFriend(id, friendId);
        log.info("Друг добавлен: userId={}, friendId={}", id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId) {

        userService.deleteFriend(id, friendId);
        log.info("Друг удален: userId={}, friendId={}", id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(
            @PathVariable Long id) {

        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getAllFriends(
            @PathVariable Long id,
            @PathVariable Long otherId) {

        return userService.getCommonFriends(id, otherId);
    }
}