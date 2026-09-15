package com.diceroller.service;

import com.diceroller.domain.user.User;
import com.diceroller.dto.request.ForgotPasswordRequestDto;
import com.diceroller.dto.request.ResetPasswordRequestDto;
import com.diceroller.dto.response.AuthResponseDto;
import com.diceroller.dto.request.LoginRequestDto;
import com.diceroller.dto.request.RegisterRequestDto;
import com.diceroller.repository.UserRepository;
import com.diceroller.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtService jwtService){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponseDto register(RegisterRequestDto register){
        if(userRepository.findByEmail(register.email()).isPresent()){
            throw new RuntimeException("Este email já esta cadastrado!");
        }

        String hashedPassword = encoder.encode(register.password());

        User user = new User(register.name(), register.email(), hashedPassword);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponseDto.of(user, token);
    }

    public AuthResponseDto login(LoginRequestDto login){
        User user = userRepository.findByEmail(login.email()).orElseThrow(() -> new RuntimeException("Email não encontrado!"));

        if(!encoder.matches(login.password(), user.getPassword())){
            throw new RuntimeException("Senha incorreta!");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponseDto.of(user, token);
    }

    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto){
        User user = userRepository.findByEmail(forgotPasswordRequestDto.email()).orElse(null);

        if(user == null){
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExp(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        //Substituir pelo serviço de email
        System.out.println("Email enviado: "+ forgotPasswordRequestDto.email());
        System.out.println("Token: "+ token);
    }

    public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto){
        User user = userRepository.findByResetToken(resetPasswordRequestDto.token()).orElseThrow(() -> new RuntimeException("Link expirado ou inválido."));
        if (user.getResetTokenExp().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link expirado!");
        }

        String hashedPassword = encoder.encode(resetPasswordRequestDto.senha());
        user.setPassword(hashedPassword);
        user.setResetTokenExp(null);
        user.setResetToken(null);
        userRepository.save(user);
    }
}
