package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
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
}