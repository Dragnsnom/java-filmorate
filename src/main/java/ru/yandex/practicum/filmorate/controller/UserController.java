package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET /api/v1/users - получение всех пользователей");
        List<User> users = userService.getAllUsers();
        log.debug("GET /api/v1/users - найдено пользователей: {}", users.size());
        return users;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("POST /api/v1/users - создание пользователя с логином: {}", user.getLogin());
        User createdUser = userService.createUser(user);
        log.info("POST /api/v1/users - пользователь создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        log.info("PUT /api/v1/users/{} - обновление пользователя", id);
        User updatedUser = userService.updateUser(user);
        log.info("PUT /api/v1/users/{} - пользователь обновлен", id);
        return updatedUser;
    }
}