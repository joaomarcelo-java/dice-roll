package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInviteRequest(@NotBlank @Size(max= 32) String name) {
}
