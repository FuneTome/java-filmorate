package ru.yandex.practicum.filmorate.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Rating;

@Component
@AllArgsConstructor
public class RatingMapper {

    public MpaDto toDto(Rating rating) {
        return new MpaDto(rating.getId(), rating.getName());
    }
}
