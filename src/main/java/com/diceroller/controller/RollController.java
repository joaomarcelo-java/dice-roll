package com.diceroller.controller;


import com.diceroller.domain.dice.Dice;
import com.diceroller.domain.dice.DiceResult;
import com.diceroller.service.DiceRollerService;
import com.diceroller.dto.request.RollRequestDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roll")
public class RollController {
    private final DiceRollerService diceRollerService;

    public RollController(DiceRollerService diceRollerService){
        this.diceRollerService = diceRollerService;
    }

    @PostMapping
    public DiceResult roll(@RequestBody @Valid RollRequestDto request){
        List<Dice> dados= request.dados().stream().map(Dice::new).toList();

        return diceRollerService.roll(dados, request.modificador());
    }
}
