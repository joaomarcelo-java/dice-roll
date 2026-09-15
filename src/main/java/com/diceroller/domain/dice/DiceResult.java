package com.diceroller.domain.dice;

import java.util.List;

public record DiceResult(List<DiceRoll> rolagens, int modificador, int total) {}