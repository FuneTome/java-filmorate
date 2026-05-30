package ru.yandex.practicum.filmorate.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortByOptionConverter implements Converter<String, SortByOption> {
    @Override
    public SortByOption convert(String source) {
        return SortByOption.valueOf(source.toUpperCase());
    }
}