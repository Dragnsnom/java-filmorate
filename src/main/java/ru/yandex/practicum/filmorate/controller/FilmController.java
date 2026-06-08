package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("GET /api/v1/films - получение всех фильмов");
        List<Film> films = filmService.getAllFilms();
        log.debug("GET /api/v1/films - найдено фильмов: {}", films.size());
        return films;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("POST /api/v1/films - добавление фильма: {}", film.getName());
        Film createdFilm = filmService.createFilm(film);
        log.info("POST /api/v1/films - фильм добавлен с id: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable Long id, @Valid @RequestBody Film film) {
        log.info("PUT /api/v1/films/{} - обновление фильма: {}", id, film.getName());
        Film updatedFilm = filmService.updateFilm(film);
        log.info("PUT /api/v1/films/{} - фильм обновлен", id);
        return updatedFilm;
    }
}