package de.hso.cardgame.model;

public record PlayerCard(Player player, Card card) {
	
	public static String toString(PlayerCard playerCard) {
		return Player.toString(playerCard.player)+";"+Card.toString(playerCard.card);
	}
	
	public static PlayerCard fromString(String str) {
		String person_str = str.split(";")[0];
		String card_str = str.split(";")[1];
		Player p = Player.fromString(person_str);
		Card c = Card.fromString(card_str);
		return new PlayerCard(p, c);
	}
	
}
