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
        return directorStorage.getDirectors().values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getDirectorById(long id) {
        if (!directorStorage.existById(id)) {
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        Director director = directorStorage.getById(id);
        return toDto(director);
    }

    public DirectorDto addDirector(DirectorRequest request) {
        Director director = toDirector(request);
        Director saved = directorStorage.addDirector(director);
        return toDto(saved);
    }

    public DirectorDto updateDirector(DirectorRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!directorStorage.existById(request.getId())) {
            throw new NotFoundException("Режиссер с id = " + request.getId() + " не найден");
        }
        Director director = toDirector(request);
        Director updated = directorStorage.updateDirector(directorStorage.getById(request.getId()), director);
        return toDto(updated);
    }

    public void deleteDirector(long id) {
        if (!directorStorage.existById(id)) {
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        directorStorage.deleteDirector(id);
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
