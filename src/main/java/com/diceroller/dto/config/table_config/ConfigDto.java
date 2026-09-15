package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConfigDto(@NotNull @Size(max = 10) List<CategoriaConfig> categorias,
                        @Size(max = 20) List<RegraConfig> regras
) {
}
