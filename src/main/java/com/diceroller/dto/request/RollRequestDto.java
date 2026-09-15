package com.diceroller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RollRequestDto(
        @JsonProperty("dados")
        @NotNull
        @Size(min=1, max=100)
        List<Integer> dados,

        @JsonProperty("modificador")
        @Min(-100) @Max(100)
        int modificador){}
