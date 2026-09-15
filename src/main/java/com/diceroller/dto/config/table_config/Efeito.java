package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;

public record Efeito(
        @NotBlank @Size(max=100) String alvo,
        @Size(max=50) String categoria,
        @Max(value = 100) @Min(value = -100) int modificador
) {
}
