package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewRequest {
    @NotBlank
    private String content;
    @JsonProperty("isPositive")
    private Boolean isPositive;
    @NotNull
    private Integer userId;
    @NotNull
    private Integer filmId;
    private Integer useful;
}
