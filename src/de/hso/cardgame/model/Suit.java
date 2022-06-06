package de.hso.cardgame.model;

public enum Suit {
	Diamonds, Hearts, Spades, Clubs;
	
	public static String toString(Suit suit) {
		if (suit == Suit.Diamonds) {
			return "Diamonds";
		} else if (suit == Suit.Hearts) {
			return "Hearts";
		} else if (suit == Suit.Spades) {
			return "Spades";
		} else if (suit == Suit.Clubs) {
			return "Clubs";
		} else {
			return null;
		}
	}
	
	public static Suit fromString(String suit) {
		if (suit.equals("Diamonds")) {
			return Suit.Diamonds;
		} else if (suit.equals("Hearts")) {
			return Suit.Hearts;
		} else if (suit.equals("Spades")) {
			return Suit.Spades;
		} else if (suit.equals("Clubs")) {
			return Suit.Clubs;
		} else {
			return null;
		}
	}
}