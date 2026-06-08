package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidDate;

@Data
public class User {

    private int id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;

    @Pattern(regexp = "\\S+", message = "Логин не может быть пустым и не должен содержать пробелы")
    private String login;
    private String name;

    @NotBlank(message = "Дата рождения не может быть пустым")
    @ValidDate
    private String birthday;
}
