package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Data
public class User {

    private long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;

    @Pattern(regexp = "\\S+", message = "Логин не может быть пустым и не должен содержать пробелы")
    private String login;
    private String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;


    public void setName(String name) {
        if (!StringUtils.hasText(name)) {
            this.name = this.login;
        } else {
            this.name = name;
        }
    }

    public void setLogin(String login) {
        this.login = login;
        if (!StringUtils.hasText(this.name)) {
            this.name = login;
        }
    }
}
