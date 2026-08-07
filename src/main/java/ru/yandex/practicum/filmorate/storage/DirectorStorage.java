package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    public List<Director> getAllDirectors();

    public Director getDirector(Long id);

    public Director addDirector(Director director);

    public Director updateDirector(Director director);

    public void deleteDirector(Long id);

    boolean existsById(int id);
}
