package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CampoConfig(@NotBlank @Size(max=24) String nome,
                          @NotNull CampoTipo tipo,
                          String dado,
                          String atributoVinculado,
                          Integer valorPadrao,
                          List<String> opcoes,
                          Integer min,
                          Integer max,
                          ConfiguracaoRolagem configuracaoRolagem
) {}