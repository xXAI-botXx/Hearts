package de.hso.cardgame.gamecentral.server;

import java.util.ArrayList;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;
import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.Suit;
import de.hso.cardgame.model.Trick;

public class GameLogic {
	private EventConsumer eventConsumer;
	private GameState state;
	private ArrayList<String> player;
	private boolean first_turn;
	private Player start_player;
	
	/**
	 * Init des Spiels
	 */
	GameLogic(GameState initialState, EventConsumer eventConsumer) {
		// Server consumed event und gibt weiter
		this.eventConsumer = eventConsumer;
		this.state = initialState;
		this.player = new ArrayList<String>();
		this.first_turn = true;
		this.start_player = Player.P1;
	}
	
	public boolean isPlayValid(Player p, Card c, GameState state) {
		if (p == this.start_player) {
			return true;
		}
		
		if (state.playerHands().get(p).hasSuit(state.currentTrick().cards().first().card().suit())) {
			if (c.suit() == state.currentTrick().cards().first().card().suit()) {
				return true;
			} else {
				System.out.println("Hand: "+state.playerHands().get(p));
				return false;
			}
		} else {
			return true;
		}
	}
	
	public Player whoTakesTrick(Trick trick) {
		// 1. get all player with the right suit
		ArrayList<PlayerCard> selection = new ArrayList<PlayerCard>();
		for (PlayerCard pc:trick.cards()) {
			if (pc.card().suit() == trick.cards().first().card().suit()) {
				selection.add(pc);
			}
		}
		// 2. calc highest player
		PlayerCard highest_play = selection.get(0);
		for (int i = 1; i < selection.size(); i++) {
			if (selection.get(i).card().rank().value() > highest_play.card().rank().value()) {
				highest_play = selection.get(i);
			}
		}
		return highest_play.player();
	}
	
	public int points_of_player(Player p, GameState state) {
		int points = 0;
		for (Card c:state.playerStacks().get(p).cards()) {
			if (c.suit() == Suit.Hearts) {
				//points += c.rank().value();
				points--;
			}
		}
		return points;
	}
	
	public Score getScore(GameState state) {
		Map<Player, Integer> score = Maps.of();
		for (Player p:Player.values()) {
			score = score.put(p, this.points_of_player(p, state));
		}
		return new Score(score);
	}
	
	public Player get_last_player() {
		if (this.start_player == Player.P1) {
			return Player.P4;
		} else if (this.start_player == Player.P2) {
			return Player.P1;
		} else if (this.start_player == Player.P3) {
			return Player.P2;
		} else if (this.start_player == Player.P4) {
			return Player.P3;
		} else {
			return null;
		}
	}
	
	public boolean gameAtTheEnd() {
		// if no player have any cards
		for (Pair<Player, Hand> pair:this.state.playerHands()) {
			if (pair.component2().cards().isEmpty() == false)
				return false;
		}
		return true;
	}
	
	public Map<Player, Hand> getPlayerHands() {
		return this.state.getPlayerHands();
	}
	
	public Player getPlayerWithClubs2() {
		for (Player p:Player.values()) {
			Hand player_hand = this.state.playerHands().get(p);
			for (Card c:player_hand.cards()) {
				if (c.suit() == Suit.Clubs && c.rank().value() == 2) {
					return p;
				}
			}
		}
		return null;
	}

	public void processCommand(GameCommand cmd) {
		String name = GameCommand.game_command_to_str(cmd);
		if (name.equals("register_player")) {
			this.player.add(name);
			RegisterPlayer real_cmd = (RegisterPlayer) cmd;
			PlayerRegistered event = new PlayerRegistered(real_cmd.p(), real_cmd.name(), this.state.playerNames());
			this.state = state.applyEvent(event);
			// send new event
			this.eventConsumer.consumeEvent(event);
		} else if (name.equals("deal_hands")) {
			System.out.println("\nCards are distributed...");
			DealHands real_cmd = (DealHands) cmd;
			// verteile Karten
			this.state = this.state.deal_hands(real_cmd.cards());
			// sende an jeden spieler, welche karten er hat
			this.eventConsumer.consumeEvent(new HandsDealt(Player.P1, this.state.playerHands().get(Player.P1)));
			this.eventConsumer.consumeEvent(new HandsDealt(Player.P2, this.state.playerHands().get(Player.P2)));
			this.eventConsumer.consumeEvent(new HandsDealt(Player.P3, this.state.playerHands().get(Player.P3)));
			this.eventConsumer.consumeEvent(new HandsDealt(Player.P4, this.state.playerHands().get(Player.P4)));
			// sende event, wer dran ist
			Player p = this.getPlayerWithClubs2();
			if (p != null) {
				this.start_player = p;
				this.state = this.state.set_start_player(p);
				this.eventConsumer.consumeEvent(new PlayerTurn(p));
			} else {
				throw new IllegalArgumentException("Tries to find the player with Club 2, but not found...");
			}
			
		} else if (name.equals("play_card")) {
			// is player on the row
			PlayCard real_cmd = (PlayCard) cmd;
			if (this.state.nextPlayer().get() == real_cmd.player()) {
				// check if first turn -> then it has to be the clubs 2
				if (this.first_turn) {
					if (real_cmd.card().suit() == Suit.Clubs && real_cmd.card().rank().value() == 2) {
						this.first_turn = false;
					} else {
						return;
					}
				}
				// set card on trick and remove card from hand
				CardPlayed event = new CardPlayed(real_cmd.player(), real_cmd.card());
				
				GameState new_state = state.applyEvent(event);
				
				// Check if is it right
				if (this.isPlayValid(real_cmd.player(), real_cmd.card(), new_state) && new_state.validate()) {
					this.state = new_state;
				} else {
					return;
				}
				
				// send new event
				this.eventConsumer.consumeEvent(event);
				
				// update player
				this.state = this.state.update_next_player();
				
				// check if 1 turn is done
				if (real_cmd.player() == this.get_last_player()) {
					// waiting
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// calc who gets the trick -> FIXME
					Trick taken_trick = this.state.currentTrick();
					Player p = this.whoTakesTrick(this.state.currentTrick());
					// set the trick to a player stack
					this.state = this.state.addStack(p, this.state.currentTrick());
					// reset trick
					this.state = this.state.cleanTrick();
					// send a event on all
					this.eventConsumer.consumeEvent(new TrickTaken(p, taken_trick, this.points_of_player(p, this.state)));
					
					// check if game at the end
					if (this.gameAtTheEnd()) {
						this.eventConsumer.consumeEvent(new GameOver(this.getScore(this.state)));
					} else {
						// update start player of the next round
						this.start_player = p;
						this.state = this.state.set_start_player(p);
						// set new playerturn
						this.eventConsumer.consumeEvent(new PlayerTurn(p));
					}
				} else {
					// set new playerturn -> not a new round
					this.eventConsumer.consumeEvent(new PlayerTurn(this.state.nextPlayer().get()));
				}
			}
		}
	}

}
