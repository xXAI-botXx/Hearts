package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.LinkedLists;
import com.github.andrewoma.dexx.collection.List;

public record Card(Suit suit, Rank rank) {
	
	public static String toString(Card c) {
		return ""+Suit.toString(c.suit) + "," + Rank.toString(c.rank)+"#";
	}
	
	public static Card fromString(String str) {
		if (str.contains("#")) {
			str = str.replaceAll("#", "");
		}
		
		if (str.contains(",") == false) {
			return null;
		} else if (str.split(",").length < 2) {
			return null;
		}
		//System.out.println("\nCard from following String: "+str);
		String suit_str = str.split(",")[0];
		String rank_str = str.split(",")[1];
		Suit suit = Suit.fromString(suit_str);
		Rank rank = Rank.fromString(rank_str);
		if (suit == null || rank == null) {
			return null;
		} else {
			return new Card(suit, rank);
		}
	}
	
	public static List<Card> wholeDeck() {
		Suit[] allSuits = Suit.values();
		List<Rank> allRanks = Rank.values();
		var builder = LinkedLists.<Card>builder();
		for (Suit s : allSuits) {
			for (Rank r : allRanks) {
				builder.add(new Card(s, r));
			}
		}
		return builder.build();
	}

}
