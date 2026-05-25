package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Map;

public interface DirectorStorage {
    Director addDirector(Director director);

    Director updateDirector(Director oldDirector, Director newDirector);

    Director getById(Long id);

    Map<Long, Director> getDirectors();

    void deleteDirector(long id);

    boolean existById(long id);
}
