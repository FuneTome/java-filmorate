package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class FilmDto {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private MpaDto mpa;
    private List<GenreDto> genres;
}