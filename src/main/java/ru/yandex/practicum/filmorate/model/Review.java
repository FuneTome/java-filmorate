package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@AllArgsConstructor
@Builder
public class Review {
    private Long id;
    private String content;
    @JsonProperty("isPositive")
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
}
