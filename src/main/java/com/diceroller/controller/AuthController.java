package com.diceroller.controller;

import com.diceroller.dto.request.ForgotPasswordRequestDto;
import com.diceroller.dto.request.ResetPasswordRequestDto;
import com.diceroller.dto.response.AuthResponseDto;
import com.diceroller.dto.request.LoginRequestDto;
import com.diceroller.dto.request.RegisterRequestDto;
import com.diceroller.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto loginRequest){
        return userService.login(loginRequest);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@RequestBody @Valid RegisterRequestDto registerRequest){
        return userService.register(registerRequest);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto forgotPasswordRequest){
        userService.forgotPassword(forgotPasswordRequest);
        return  ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto resetPasswordRequest){
        userService.resetPassword(resetPasswordRequest);
        return  ResponseEntity.ok().build();
    }
}
