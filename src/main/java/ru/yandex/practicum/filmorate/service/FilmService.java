package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Service
public class FilmService {
    private static final String SEARCH_BY_TITLE = "title";
    private static final String SEARCH_BY_DIRECTOR = "director";

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage  directorStorage;
    private final EventStorage eventStorage;

    public FilmService(
            @org.springframework.beans.factory.annotation.Qualifier("filmDbStorage") FilmStorage filmStorage,
            @org.springframework.beans.factory.annotation.Qualifier("userDbStorage") UserStorage userStorage,
            @org.springframework.beans.factory.annotation.Qualifier("directorDbStorage") DirectorStorage directorStorage,
            EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
        this.eventStorage = eventStorage;
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getUser(userId);
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.getFilm(filmId);
    }

    public Film createFilm(Film film) {
        validateDirectors(film);
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
        eventStorage.addEvent(userId, ru.yandex.practicum.filmorate.model.EventType.LIKE, ADD, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        filmStorage.removeLike(film, user);
        eventStorage.addEvent(userId, ru.yandex.practicum.filmorate.model.EventType.LIKE, REMOVE, filmId);
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

    public List<Film> searchFilms(String query, String by) {
        if (query == null || by == null) {
            throw new ValidationException("Параметры query и by обязательны");
        }

        Set<String> criteria = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(criterion -> !criterion.isEmpty())
                .collect(Collectors.toSet());

        Set<String> unknown = new HashSet<>(criteria);
        unknown.removeAll(Set.of(SEARCH_BY_TITLE, SEARCH_BY_DIRECTOR));
        if (!unknown.isEmpty()) {
            throw new ValidationException("Параметр by может принимать значения "
                    + SEARCH_BY_TITLE + " и " + SEARCH_BY_DIRECTOR + ", получено: " + by);
        }

        return filmStorage.searchFilms(
                query,
                criteria.contains(SEARCH_BY_TITLE),
                criteria.contains(SEARCH_BY_DIRECTOR));
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
