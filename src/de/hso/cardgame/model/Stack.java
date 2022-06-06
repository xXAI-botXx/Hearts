package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.Set;

/**
 * Die während des Spiels erzielten Karten einer Spielerin.
 */
public record Stack(Set<Card> cards) {
	// Punkte berechnen
	public static int calculatePoints(Set<Card> card) {
		int score = 0;
		for (Card c: card) {
			if (c.suit() == Suit.Hearts) {
				score--;
			}
		}
		return score;
	}
  
  
}
