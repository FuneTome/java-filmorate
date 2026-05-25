package ru.yandex.practicum.filmorate.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.SearchBy;

import java.util.Arrays;

@Component
public class StringToSearchByConverter implements Converter<String, SearchBy> {

    @Override
    public SearchBy convert(@NonNull String source) {
        return Arrays.stream(SearchBy.values())
                .filter(v -> v.toString().equalsIgnoreCase(source))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown value: " + source));
    }
}
