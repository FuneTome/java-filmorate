package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final RatingDbStorage ratingDbStorage;

    public List<MpaDto> getAllRatings() {
        return ratingDbStorage.findAll();
    }

    public MpaDto getRatingById(int id) {
        return ratingDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }
}