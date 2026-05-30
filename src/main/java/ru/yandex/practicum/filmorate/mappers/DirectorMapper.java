package ru.yandex.practicum.filmorate.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Component
@AllArgsConstructor
public class DirectorMapper {

    public DirectorDto toDto(Director director) {
        return new DirectorDto(director.getId(), director.getName());
    }

}
