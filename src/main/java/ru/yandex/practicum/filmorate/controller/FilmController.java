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
        log.info("GET /films - получение всех фильмов");
        List<Film> films = filmService.getAllFilms();
        log.debug("GET /films - найдено фильмов: {}", films.size());
        return films;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("POST /films - добавление фильма: {}", film.getName());
        Film createdFilm = filmService.createFilm(film);
        log.info("POST /films - фильм добавлен с id: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("PUT /films - обновление фильма: {}", film.getName());
        Film updatedFilm = filmService.updateFilm(film);
        log.info("PUT /films - фильм обновлен");
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") Long count) {
        return filmService.getPopularFilms(count);
    }
}