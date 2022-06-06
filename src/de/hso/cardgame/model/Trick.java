package de.hso.cardgame.model;

import java.util.Optional;

import com.github.andrewoma.dexx.collection.LinkedLists;
import com.github.andrewoma.dexx.collection.List;

public record Trick(List<PlayerCard> cards) {

	public static Trick empty = new Trick(LinkedLists.of());
	
	public Trick addCard(Player p, Card c) {
		return new Trick(this.cards.append(new PlayerCard(p, c)));
	}
	
	public static String toString(Trick trick) {
		String str = "";
		for (PlayerCard pc:trick.cards()) {
			str += ""+PlayerCard.toString(pc);//+"#";
		}
		return str;
	}
	
	public static Trick fromString(String str) {
		List<PlayerCard> player_cards = LinkedLists.of();
		for (String s:str.split("#")) {
			Player p = Player.fromString(s.split(";")[0]);
			Card c = Card.fromString(s.split(";")[1]);
			player_cards = player_cards.append(new PlayerCard(p, c));
		}
		
		return new Trick(player_cards);
	}
	
	public Optional<Suit> leadingSuit() {
	if (cards.size() == 0) {
		return Optional.empty();
	} else {
		return Optional.of(cards.get(0).card().suit());
		}
	}
	
}
