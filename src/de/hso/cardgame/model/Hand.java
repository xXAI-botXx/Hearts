package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.HashSet;
import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;

/**
 * Die Karten auf der Hand einer Spielerin.
 */
public record Hand(Set<Card> cards) {
	
	public static String toString(Hand hand) {
		// #Ace;Hearts#2;Caro
		String result = "";
		for (Card c: hand.cards()) {
			result += Card.toString(c)+"";
		}
		//result = result.substring(0, result.length() - 1);
		
		return result;
	}
	
	public static Hand fromString(String str) {
		String[] cards = str.split("#");
		Set<Card> card_set = Sets.of();
		for (int i = 0; i < cards.length; i++) {
			Card card = Card.fromString(cards[i]);
			card_set = card_set.add(card);
		}
		return new Hand(card_set);
	}

	public boolean hasCard(Card c) {
		return this.cards.contains(c);
	}
	
	public boolean hasSuit(Suit s) {
		for (Card c:this.cards) {
			if (c.suit() == s) {
				return true;
			}
		}
		return false;
	}

}
