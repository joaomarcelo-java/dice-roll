package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CategoriaConfig(@NotBlank @Size(max = 50) String nome,
                              @NotNull CategoriaMetodo metodo,
                              Integer pontosDisponiveis,
                              String dadoCriacao,
                              @NotNull CategoriaTipo tipo,
                              @NotNull @Size(max = 30) List<CampoConfig> campos,
                              ConfiguracaoRolagem configuracaoRolagem) {
}
