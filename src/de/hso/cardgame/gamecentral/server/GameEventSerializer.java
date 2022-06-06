package de.hso.cardgame.gamecentral.server;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.Trick;
import de.hso.cardgame.model.GameEvent.*;
import de.hso.cardgame.model.Hand;

import java.util.ArrayList;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;

public class GameEventSerializer {
	//Player player, String name, Map<Player, String> otherPlayers
	/* GameEvent as String-Object:
	 * 
	 * Playeregistered:
	 * "player_registered P3 Tobia P1:Daniela,P2:Syon"
	 * 
	 **/
	
	// Event Strings = EventName *JSON-String
	// Eventname are case-stable and have underlines
	
	public static String toString(GameEvent event) {
		// "player_registered P3 Tobia P1:Daniela,P2:Syon"
		
		String str = GameEventSerializer.from_gameevent_to_string(event)+" ";
		if (event instanceof PlayerRegistered pr) {
			
			String otherPlayers = "";
			for (Pair<Player, String> pair:pr.otherPlayers()) {
				otherPlayers += Player.toString(pair.component1())+":"+pair.component2()+",";
			}
			if (otherPlayers.length() > 1)
				otherPlayers = otherPlayers.substring(0, otherPlayers.length() - 1);
			
			str += Player.toString(pr.player())+" "+pr.name()+" "+otherPlayers;
		} else if (event instanceof HandsDealt hd) {
			str += Player.toString(hd.player())+" "+Hand.toString(hd.hand());
		} else if (event instanceof PlayerTurn pt) {
			str += Player.toString(pt.player());
		} else if (event instanceof CardPlayed cp) {
			str += Player.toString(cp.player());
			str += " ";
			str += Card.toString(cp.card());
		} else if (event instanceof TrickTaken tt) {
			str += Player.toString(tt.player());
			str += " ";
			str += Trick.toString(tt.trick());
			str += " ";
			str += tt.points();
		} else if (event instanceof GameError ge) {
			str += ge.msg();
		} else if (event instanceof PlayerError pe) {
			str += Player.toString(pe.player());
			str += " ";
			str += pe.msg();
		} else if (event instanceof GameOver go) {
			str += Score.toString(go.score());
		}
		return str;
	}
	
	public static GameEvent fromString(String event_str) {
		String[] args = event_str.split(" "); 
		String event_name = args[0];
		
		// "player_registered P3 Tobia P1:Daniela,P2:Syon"
		GameEvent event = null;
		if (event_name.equalsIgnoreCase("player_registered")) {
			Player p = Player.fromString(args[1]);
			String name = args[2];
			Map<Player, String> otherPlayers = Maps.of();
			if (args.length > 3) {
				for (String entry:args[3].split(",")) {
					Player p_new = Player.fromString(entry.split(":")[0]);
					String name_new = entry.split(":")[1]; 
					otherPlayers = otherPlayers.put(p_new, name_new);
				}
			}
			event = new PlayerRegistered(p, name, otherPlayers);
		} else if (event_name.equalsIgnoreCase("hands_dealt")) {
			Player p = Player.fromString(args[1]);
			Hand hand = Hand.fromString(args[2]);
			event = new HandsDealt(p, hand);
		} else if (event_name.equalsIgnoreCase("player_turn")) {
			Player p = Player.fromString(args[1]);
			event = new PlayerTurn(p);
		} else if (event_name.equalsIgnoreCase("card_played")) {
			Player p = Player.fromString(args[1]);
			Card c = Card.fromString(args[2]);
			event = new CardPlayed(p, c);
		} else if (event_name.equalsIgnoreCase("trick_taken")) {
			Player p = Player.fromString(args[1]);
			Trick trick = Trick.fromString(args[2]);
			int points = Integer.parseInt(args[3]);
			event = new TrickTaken(p, trick, points);
		} else if (event_name.equalsIgnoreCase("game_error")) {
			String msg = "";
			for (int i = 1; i < args.length; i++) {
				msg += " "+args[i];
			}
			event = new GameError(msg);
		} else if (event_name.equalsIgnoreCase("player_error")) {
			Player p = Player.fromString(args[1]);
			String msg = "";
			for (int i = 2; i < args.length; i++) {
				msg += " "+args[i];
			}
			event = new PlayerError(p, msg);
		} else if (event_name.equalsIgnoreCase("game_over")) {
			Score s = Score.fromString(args[1]);
			event = new GameOver(s);
		}
			
		return event;
	}
	
	public static String from_gameevent_to_string(GameEvent event) {
		if (event.getClass() == PlayerRegistered.class) {
			return "player_registered";
		} else if (event.getClass() == HandsDealt.class) {
			return "hands_dealt";
		} else if (event.getClass() == PlayerTurn.class) {
			return "player_turn";
		} else if (event.getClass() == CardPlayed.class) {
			return "card_played";
		} else if (event.getClass() == TrickTaken.class) {
			return "trick_taken";
		} else if (event.getClass() == GameError.class) {
			return "game_error";
		} else if (event.getClass() == PlayerError.class) {
			return "player_error";
		} else if (event.getClass() == GameOver.class) {
			return "game_over";
		} else {
			return null;
		}
	}

}
