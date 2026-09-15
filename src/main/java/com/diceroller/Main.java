package com.diceroller;

import com.diceroller.domain.dice.Dice;
import com.diceroller.domain.dice.DiceResult;
import com.diceroller.domain.dice.DiceRoll;
import com.diceroller.service.DiceRollerService;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Dice> dados = new ArrayList<>();
        DiceRollerService rolador = new DiceRollerService();

        dados.add(new Dice(15));
        dados.add(new Dice(20));
        dados.add(new Dice(30));

        DiceResult results = rolador.roll(dados, -6);

        for(DiceRoll d : results.rolagens()) {
            System.out.println(d.dice().faces() + " -> " + d.valor());
        }

        if(results.modificador() < 0) {
            System.out.println("MODIFICADOR: " + results.modificador());
        }else if(results.modificador() > 0) {
            System.out.println("MODIFICADOR: +" + results.modificador());
        }else {
            System.out.println("MODIFICADOR: "+ results.modificador());
        }

        System.out.println("TOTAL = " + results.total());
    }
}
