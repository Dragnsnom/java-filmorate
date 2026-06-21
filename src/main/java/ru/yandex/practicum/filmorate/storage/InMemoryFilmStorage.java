package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @Override
    public Film create(Film film) {
        log.debug("Создание нового фильма: {}", film.getName());
        film.setId(currentId++);
        films.put(film.getId(), film);

        log.info("Фильм создан: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма: id={}, name={}", film.getId(), film.getName());
        getFilmOrThrow(film.getId());
        films.put(film.getId(), film);

        log.info("Фильм обновлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public List<Film> getAll() {
        log.debug("Получение всех фильмов");
        List<Film> allFilms = new ArrayList<>(films.values());

        log.debug("Найдено фильмов: {}", allFilms.size());
        return allFilms;
    }

    @Override
    public Film getFilm(Long id) {
        log.debug("получение фильма: id={}, name={}", films.get(id).getId(), films.get(id).getName());
        return films.get(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);
        Film film = getFilmOrThrow(filmId);

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new IllegalStateException("Пользователь уже поставил лайк этому фильму");
        }

        film.addLike(userId);
        log.info("Лайк добавлен: filmId={}, userId={}, всего лайков={}",
                filmId, userId, film.getLikes().size());
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);
        Film film = getFilmOrThrow(filmId);

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        film.removeLike(userId);
        log.info("Лайк удален: filmId={}, userId={}, осталось лайков={}",
                filmId, userId, film.getLikes().size());
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        log.debug("Запрос популярных фильмов: count={}", count);

        List<Film> popularFilms = films.values().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .toList();

        log.debug("Найдено популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }

    private Film getFilmOrThrow(Long id) {
        return Optional.ofNullable(films.get(id))
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }
}