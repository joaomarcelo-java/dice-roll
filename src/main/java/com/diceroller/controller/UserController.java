package com.diceroller.controller;

import com.diceroller.domain.user.User;
import com.diceroller.dto.response.MeResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public MeResponseDto me(@AuthenticationPrincipal User currentUser) {
        return MeResponseDto.from(currentUser);
    }
}