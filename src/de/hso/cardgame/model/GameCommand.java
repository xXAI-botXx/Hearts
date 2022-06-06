package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.Map;

public sealed interface GameCommand 
	permits GameCommand.DealHands, GameCommand.PlayCard, GameCommand.RegisterPlayer 
{

	/**
	 * Anweisung (von einer Spielerin): ich möchte am Spiel teilnehmen
	 */
	public static record RegisterPlayer(Player p, String name) implements GameCommand {}
	
	/**
	 * Anweisung (von extern): die Karten sollen ausgeteilt werden.
	 */
	public static record DealHands(Map<Player, Hand> cards) implements GameCommand {}
	
	/**
	 * Anweisung (von einer Spielerin): ich (Spielerin player) spiele Karte card aus. 
	 */
	public static record PlayCard(Player player, Card card) implements GameCommand {}
	
	public static String game_command_to_str(GameCommand command) {
		if (command.getClass() == RegisterPlayer.class) {
			return "register_player";
		} else if (command.getClass() == DealHands.class) {
			return "deal_hands";
		} else if (command.getClass() == PlayCard.class) {
			return "play_card";
		} else {
			return null;
		}
	}
}
