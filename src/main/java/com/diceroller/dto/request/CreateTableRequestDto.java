package com.diceroller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTableRequestDto(@NotBlank @Size(max=64) String name,
                                    @Size(max = 500) String description) {
}
