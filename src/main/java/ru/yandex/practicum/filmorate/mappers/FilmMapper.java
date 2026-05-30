package ru.yandex.practicum.filmorate.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FilmMapper {
    private final RatingMapper ratingMapper;
    private final GenreMapper genreMapper;
    private final DirectorMapper directorMapper;

    public Film toFilm(FilmRequest request) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null) {
            Rating rating = new Rating();
            rating.setId(request.getMpa().getId());
            film.setRating(rating);
        }
        if (request.getGenres() != null) {
            Set<Genre> genres = request.getGenres().stream()
                    .map(g -> {
                        Genre genre = new Genre();
                        genre.setId(g.getId());
                        return genre;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(genres);
        }
        if (request.getDirectors() != null) {
            Set<Director> directors = request.getDirectors().stream()
                    .map(g -> {
                        Director director = new Director();
                        director.setId(g.getId());
                        return director;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setDirector(directors);
        }
        return film;
    }

    public FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getRating() != null) {
            MpaDto mpaDto = ratingMapper.toDto(film.getRating());
            dto.setMpa(mpaDto);
        }

        List<GenreDto> genreDtos = film.getGenres().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
        dto.setGenres(genreDtos);

        List<DirectorDto> directorDtos = film.getDirector().stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toList());
        dto.setDirectors(directorDtos);

        return dto;
    }
}
