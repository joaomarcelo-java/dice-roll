package com.diceroller.service;

import com.diceroller.domain.dice.Dice;
import com.diceroller.domain.dice.DiceResult;
import com.diceroller.domain.dice.DiceRoll;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.util.Random;
@Service
public class DiceRollerService {
	private Random rand = new Random();	
	
	public DiceResult roll(List<Dice> Dices, int modificador){
		List<DiceRoll> rolledDices = new ArrayList<>();
		int total = modificador;
		for(Dice d : Dices) {
			int valorRodado = rand.nextInt(d.faces()) + 1;
			rolledDices.add(new DiceRoll(d, valorRodado));
			total += valorRodado;
		}
		DiceResult dicesResults = new DiceResult(rolledDices, modificador, total);
		
		return dicesResults;
	}

	public DiceResult roll(String notacao){
		String[] partes = notacao.toLowerCase().split("d");
		int quantidade = 1;
		int faces;
		try {
			if (partes.length == 1) {
				faces = Integer.parseInt(partes[0]);
			} else {
				quantidade = Integer.parseInt(partes[0]);
				faces = Integer.parseInt(partes[1]);
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException("Notação de dado inválida: " + notacao);
		}

		List<Dice> dados = new ArrayList<>();
		for (int i = 0; i < quantidade; i++) dados.add(new Dice(faces));
		return roll(dados, 0);
	}
	
}