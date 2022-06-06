package de.hso.cardgame.gamecentral.server;

import java.util.ArrayList;
import java.util.Optional;

import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;
import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Stack;
import de.hso.cardgame.model.Trick;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameError;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerError;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

record GameState(
		Map<Player, String> playerNames,
		Map<Player, Hand> playerHands,
		Map<Player, Stack> playerStacks,
		Trick currentTrick,
		Optional<Player> nextPlayer
	) {
	
	public static final GameState empty = new GameState(Maps.of(), Maps.of(), Maps.of(), Trick.empty, Optional.of(Player.P1));

	public boolean validate() {
		Set<Card> all_cards = Sets.of();
		// proof, that no card has a duplicate one
			// check hands
		for (Pair<Player,Hand> pair:this.playerHands) {
			for (Card c:pair.component2().cards()) {
				if (all_cards.contains(c)) {
					return false;
				}
				all_cards = all_cards.add(c);
			}
		}
			// check stacks
		for (Pair<Player, Stack> pair:this.playerStacks) {
			for (Card c:pair.component2().cards()) {
				if (all_cards.contains(c)) {
					return false;
				}
				all_cards = all_cards.add(c);
			}
		}
			// check trick
		for (PlayerCard pc:this.currentTrick.cards()) {
			Card c = pc.card();
			if (all_cards.contains(c)) {
				return false;
			}
			all_cards = all_cards.add(c);
		}
		return true;
	}
	
	public GameState set_start_player(Player p) {
		Optional<Player> new_nextPlayer = Optional.of(p);
		return new GameState(this.playerNames, this.playerHands, this.playerStacks, this.currentTrick, new_nextPlayer);
	}
	
	public GameState update_next_player() {
		Optional<Player> new_nextPlayer = Optional.empty();
		if (this.nextPlayer.get() == Player.P1) {
			new_nextPlayer = Optional.of(Player.P2);
		} else if (this.nextPlayer.get() == Player.P2) {
			new_nextPlayer = Optional.of(Player.P3);
		} else if (this.nextPlayer.get() == Player.P3) {
			new_nextPlayer = Optional.of(Player.P4);
		} else if (this.nextPlayer.get() == Player.P4) {
			new_nextPlayer = Optional.of(Player.P1);
		}
		return new GameState(this.playerNames, this.playerHands, this.playerStacks, this.currentTrick, new_nextPlayer);
	}
	
	public GameState deal_hands(Map<Player, Hand> hands) {
		// init Hand and Stack
		Map<Player, Stack> stacks = this.playerStacks;
		if (hands == null || hands.get(Player.P1) == null) {
			hands = Maps.of();
			hands = hands.put(Player.P1, new Hand(Sets.of()));
			hands = hands.put(Player.P2, new Hand(Sets.of()));
			hands = hands.put(Player.P3, new Hand(Sets.of()));
			hands = hands.put(Player.P4, new Hand(Sets.of()));
			
			stacks = Maps.of();
			stacks = stacks.put(Player.P1, new Stack(Sets.of()));
			stacks = stacks.put(Player.P2, new Stack(Sets.of()));
			stacks = stacks.put(Player.P3, new Stack(Sets.of()));
			stacks = stacks.put(Player.P4, new Stack(Sets.of()));
		}
		// get all cards in a set
		List<Card> cards_left = Card.wholeDeck();
		// get all indexes
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < cards_left.size(); i++) {
			if (cards_left.get(i) != null) {
				indexes.add(i);
			}
		}
		// distribute cards
		while (indexes.size() > 0) {
			for (Player p:Player.values()) {
				
				// get new random index / card
				int rand_i = (int) (Math.random() * (indexes.size() - 1));
				
				Card c = cards_left.get(indexes.get(rand_i));
				Hand hand = hands.get(p);
				Hand new_hand = new Hand(hand.cards().add(c));
				hands = hands.put(p, new_hand);
				// delete card out of list
				//cards_left = cards_left.drop(indexes.get(rand_i));
				indexes.remove(rand_i);
				
				if (indexes.size() < 1) {
					break;
				}
			}
		}
		return new GameState(this.playerNames(), hands, stacks, this.currentTrick(), this.nextPlayer());
	}
	
	public Map<Player, Hand> getPlayerHands() {
		return this.playerHands;
	}
	
	public GameState addStack(Player p, Trick trick) {
		Map<Player, Stack> new_playerStacks = this.playerStacks;
		for (PlayerCard pc:trick.cards()) {
			Card card = pc.card();
			//new_playerStacks = this.playerStacks.remove(p);
			new_playerStacks = new_playerStacks.put(p, new Stack(new_playerStacks.get(p).cards().add(card)));
		}
		System.out.println("New Stack: "+new_playerStacks);
		return new GameState(this.playerNames, this.playerHands, new_playerStacks, this.currentTrick, this.nextPlayer);
	}
	
	public GameState cleanTrick() {
		return new GameState(this.playerNames, this.playerHands, this.playerStacks, Trick.empty, this.nextPlayer);
	}
	
	GameState applyEvent(GameEvent event) {
		if (event.getClass() == PlayerRegistered.class) {
			PlayerRegistered real_event = (PlayerRegistered) event;
			Map<Player, String> new_playerNames = this.playerNames.put(real_event.player(), real_event.name());
			return new GameState(new_playerNames, this.playerHands, this.playerStacks, this.currentTrick, this.nextPlayer);
		} else if (event.getClass() == HandsDealt.class) {
			return null;
		} else if (event.getClass() == PlayerTurn.class) {
			return null;
		} else if (event.getClass() == CardPlayed.class) {
			CardPlayed real_event = (CardPlayed) event;
			Trick new_trick = this.currentTrick().addCard(real_event.player(), real_event.card());
			Map<Player, Hand> new_hands = this.playerHands.put(real_event.player(), new Hand(this.playerHands.get(real_event.player()).cards().remove(real_event.card())));
			return new GameState(this.playerNames, new_hands, this.playerStacks, new_trick, this.nextPlayer);
		} else if (event.getClass() == TrickTaken.class) {
			TrickTaken real_event = (TrickTaken) event;
			GameState new_state = this.addStack(real_event.player(), real_event.trick());
			return new_state;
		} else if (event.getClass() == GameError.class) {
			return null;
		} else if (event.getClass() == PlayerError.class) {
			return null;
		} else if (event.getClass() == GameOver.class) {
			Optional<Player> new_nextPlayer = Optional.empty();
			return null;
		} else {
			return null;
		}
	}
	
}
