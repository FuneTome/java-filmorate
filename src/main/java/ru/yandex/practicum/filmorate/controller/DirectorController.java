package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.DirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<DirectorDto> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto addDirector(@Valid @RequestBody DirectorRequest request) {
        return directorService.addDirector(request);
    }

    @PutMapping
    public DirectorDto updateDirector(@Valid @RequestBody DirectorRequest request) {
        return directorService.updateDirector(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable long id) {
        directorService.deleteDirector(id);
    }
}
