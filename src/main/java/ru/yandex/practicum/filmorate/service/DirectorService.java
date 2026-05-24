package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.DirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<DirectorDto> getDirectors() {
        log.info("Запрос на получение всех режиссёров");
        return directorStorage.getDirectors().values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getDirectorById(long id) {
        log.info("Запрос на получение режиссёра по id: {}", id);
        if (!directorStorage.existById(id)) {
            log.warn("Режиссёр с id {} не найден", id);
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        Director director = directorStorage.getById(id);
        return toDto(director);
    }

    public DirectorDto addDirector(DirectorRequest request) {
        log.info("Запрос на добавление нового режиссёра: {}", request.getName());
        Director director = toDirector(request);
        Director saved = directorStorage.addDirector(director);
        log.info("Режиссёр успешно добавлен с id: {}", saved.getId());
        return toDto(saved);
    }

    public DirectorDto updateDirector(DirectorRequest request) {
        log.info("Запрос на обновление режиссёра с id: {}", request.getId());
        if (request.getId() == null) {
            log.warn("Попытка обновления режиссёра без указания id");
            throw new ValidationException("Id должен быть указан");
        }
        if (!directorStorage.existById(request.getId())) {
            log.warn("Режиссёр с id {} не найден для обновления", request.getId());
            throw new NotFoundException("Режиссер с id = " + request.getId() + " не найден");
        }
        Director director = toDirector(request);
        Director updated = directorStorage.updateDirector(directorStorage.getById(request.getId()), director);
        log.info("Режиссёр с id {} успешно обновлён", updated.getId());
        return toDto(updated);
    }

    public void deleteDirector(long id) {
        log.info("Запрос на удаление режиссёра с id: {}", id);
        if (!directorStorage.existById(id)) {
            log.warn("Попытка удаления несуществующего режиссёра с id {}", id);
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        directorStorage.deleteDirector(id);
        log.info("Режиссёр с id {} успешно удалён", id);
    }

    public boolean existsById(long id) {
        return directorStorage.existById(id);
    }

    private Director toDirector(DirectorRequest request) {
        Director director = new Director();
        director.setId(request.getId());
        director.setName(request.getName());
        return director;
    }

    private DirectorDto toDto(Director director) {
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getId());
        dto.setName(director.getName());
        return dto;
    }
}