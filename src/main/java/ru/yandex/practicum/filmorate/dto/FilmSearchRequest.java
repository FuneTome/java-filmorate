package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.SearchBy;

import java.util.Set;

@Data
public class FilmSearchRequest {
    @NotBlank
    String query;
    @NotEmpty
    Set<SearchBy> by;
}
