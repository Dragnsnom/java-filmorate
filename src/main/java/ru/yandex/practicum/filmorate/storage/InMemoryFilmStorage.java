package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

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
        getFilmOrThrow(id);
        log.debug("получение фильма: id={}, name={}", films.get(id).getId(), films.get(id).getName());
        return films.get(id);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.debug("Запрос общих фильмов: userId={}, friendId={}", userId, friendId);
        List<Film> commonFilms = films.values().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .toList();

        log.debug("Найдено общих фильмов: {}", commonFilms.size());
        return commonFilms;
    }

    @Override
    public void addLike(Film film, User user) {
        log.debug("Добавление лайка: film={}, user={}", film.getId(), user.getId());

        if (film.getLikes().contains(user.getId())) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", user.getId(), film.getId());
            throw new DuplicateLikeException("Пользователь уже поставил лайк этому фильму");
        }

        film.addLike(user.getId());
        log.info("Лайк добавлен: filmId={}, userId={}, всего лайков={}",
                film.getId(), user.getId(), film.getLikes().size());
    }

    @Override
    public void removeLike(Film film, User user) {
        log.debug("Удаление лайка: film={}, user={}", film.getId(), user.getId());

        if (!film.getLikes().contains(user.getId())) {
            log.warn("Пользователь {} не ставил лайк фильму {}", user.getId(), film.getId());
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        film.removeLike(user.getId());
        log.info("Лайк удален: filmId={}, userId={}, осталось лайков={}",
                film.getId(), user.getId(), film.getLikes().size());
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

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        log.debug("Получение фильмов режиссёра: directorId={}, sortBy={}", directorId, sortBy);

        Comparator<Film> comparator;

        if ("year".equals(sortBy)) {
            comparator = Comparator
                    .comparing(Film::getReleaseDate)
                    .thenComparing(Film::getId);
        } else if ("likes".equals(sortBy)) {
            comparator = Comparator
                    .comparingInt(Film::getLikesCount)
                    .reversed()
                    .thenComparing(Film::getId);
        } else {
            throw new IllegalArgumentException("Параметр sortBy должен иметь значение year или likes");
        }

        List<Film> result = films.values().stream()
                .filter(film -> film.getDirectors() != null)
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(director -> director != null
                                && director.getId() == directorId))
                .sorted(comparator)
                .toList();

        log.debug("Для режиссёра id={} найдено фильмов: {}", directorId, result.size());

        return result;
    }

    @Override
    public List<Film> searchFilms(String query, boolean byTitle, boolean byDirector) {
        log.debug("Поиск фильмов: query={}, byTitle={}, byDirector={}", query, byTitle, byDirector);

        if (!byTitle && !byDirector) {
            return List.of();
        }

        String pattern = query.toLowerCase();

        List<Film> found = films.values().stream()
                .filter(film -> (byTitle && matchesTitle(film, pattern))
                        || (byDirector && matchesDirector(film, pattern)))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed()
                        .thenComparing(Film::getId))
                .toList();

        log.debug("Найдено фильмов по запросу '{}': {}", query, found.size());
        return found;
    }

    private boolean matchesTitle(Film film, String pattern) {
        return film.getName() != null && film.getName().toLowerCase().contains(pattern);
    }

    private boolean matchesDirector(Film film, String pattern) {
        if (film.getDirectors() == null) {
            return false;
        }
        return film.getDirectors().stream()
                .anyMatch(director -> director != null
                        && director.getName() != null
                        && director.getName().toLowerCase().contains(pattern));
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        // Заглушка, так как рекомендации обычно требуют БД
        return List.of();
    }

    private void getFilmOrThrow(Long id) {
        Optional.ofNullable(films.get(id))
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }
}