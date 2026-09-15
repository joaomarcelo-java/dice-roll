package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateInviteRequestDto(
        @NotNull InviteAction status
) {}