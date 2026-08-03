package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getAll();

    Film getFilm(Long id);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getPopularFilms(Long count);

    List<Film> searchByTitle(String query);

    List<Film> getRecommendations(Long userId);
}
