package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return  filmService.getAllFilms();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping("/{id}")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }
}
