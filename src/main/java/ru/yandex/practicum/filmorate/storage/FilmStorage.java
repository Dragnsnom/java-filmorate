package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getAll();

    Film getFilm(Long id);

    List<Film> getCommonFilms(Long userId, Long friendId);

    boolean addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getPopularFilms(Long count, Integer genreId, Integer year);

    List<Film> getFilmsByDirector(int directorId, String sortBy);

    List<Film> searchFilms(String query, boolean byTitle, boolean byDirector);

    List<Film> getRecommendations(Long userId);

    void deleteFilm(Long id);
}
