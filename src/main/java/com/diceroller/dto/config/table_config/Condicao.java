package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotBlank;

public record Condicao(@NotBlank String campo,
                       @NotBlank String valor) {
}
