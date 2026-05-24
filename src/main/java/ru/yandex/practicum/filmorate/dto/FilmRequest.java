package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmRequest {
    private Long id;
    @NotNull @NotBlank
    private String name;
    @NotNull @NotBlank @Size(min = 1, max = 200)
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private MpaRequest mpa;
    private List<GenreRequest> genres;
    private List<DirectorRequest> directors;
}