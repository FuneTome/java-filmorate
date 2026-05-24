package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class ReviewUpdateDto {
    @NotNull
    @Positive
    private Long reviewId;
    private String content;
    @JsonProperty("isPositive")
    private Boolean isPositive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
}
