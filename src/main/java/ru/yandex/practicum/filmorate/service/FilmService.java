package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(
            @org.springframework.beans.factory.annotation.Qualifier("filmDbStorage") FilmStorage filmStorage,
            @org.springframework.beans.factory.annotation.Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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

    public List<Film> searchFilms(String query, String by) {
        List<String> criteria = List.of(by.split(","));
        // сейчас поддерживается только поиск по названию: режиссёров в проекте нет
        if (criteria.contains("title")) {
            return filmStorage.searchByTitle(query);
        }
        return List.of();
    }
}
