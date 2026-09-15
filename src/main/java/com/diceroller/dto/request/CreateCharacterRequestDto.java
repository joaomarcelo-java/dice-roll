package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateCharacterRequestDto(
        @NotBlank @Size(max = 24) String characterName,
        @NotNull Map<String, Map<String, Object>> data
) {
}
