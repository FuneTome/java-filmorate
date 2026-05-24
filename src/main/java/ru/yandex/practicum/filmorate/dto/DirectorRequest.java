package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectorRequest {
    private Long id;
    @NotBlank
    private String name;
}
