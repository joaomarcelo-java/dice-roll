package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegraConfig (@NotNull Condicao condicao,
                          @NotNull List<Efeito> efeitos){
}
