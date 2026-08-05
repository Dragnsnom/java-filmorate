package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage  directorStorage;

    public FilmService(
            @org.springframework.beans.factory.annotation.Qualifier("filmDbStorage") FilmStorage filmStorage,
            @org.springframework.beans.factory.annotation.Qualifier("userDbStorage") UserStorage userStorage,
            @org.springframework.beans.factory.annotation.Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getUser(userId);
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.getFilm(filmId);
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        validateDirectors(film);
        return filmStorage.update(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(Long filmId, Long userId) {
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        filmStorage.addLike(film, user);
    }

    public void removeLike(Long filmId, Long userId) {
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        filmStorage.removeLike(film, user);
    }

    public List<Film> getPopularFilms(Long count) {
       return filmStorage.getPopularFilms(count);
    }

    public Film getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Параметр sortBy должен иметь значение year или likes");
        }

        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    private void validateDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        for (Director director : film.getDirectors()) {
            if (director == null) {
                throw new ValidationException("Режиссёр не может быть null");
            }

            if (!directorStorage.existsById(director.getId())) {
                throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
            }
        }
    }
}
