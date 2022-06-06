package de.hso.cardgame.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.andrewoma.dexx.collection.Map;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameError;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerError;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.Suit;
import de.hso.cardgame.ui.Client;
import de.hso.cardgame.ui.UI;

public class DumbAI implements UI{
	
	private Queue event_q;
	private	Client client;
	private boolean should_run;
	
	private String host;
	private int port;
	private String name;
	
	public DumbAI(String host, int port, String name) {
		this.host = host;
		this.port = port;
		this.name = name;
		
		this.should_run = true;
		
		this.event_q = new ConcurrentLinkedQueue<GameEvent>();
	}
	
	public void run() {
		boolean connected = this.connect_to_server(this.host, this.port, this.name);
		
		while(this.should_run && connected) {
			if (event_q.isEmpty() == false) {
		    	   process_event((GameEvent) event_q.poll());
		    }
		}
	}
	
	private boolean connect_to_server(String host, int port, String name) {
		try {
			this.client = new Client(host, port);
			this.set_ui();
	        this.start_hearing();
	        this.register();
	        this.client.setName(name);
	        System.out.println("KI runs now on: "+port);
			return true;
		}  catch (IOException e) {
			System.out.println("No server on this port found...");
			return false;
		}
	}
	
	private void set_ui() {
		this.client.setUI(this);
	}
	
	private void register() {
		try {
			this.client.register(this.name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void start_hearing() {
		new Thread(() -> {
			try {
				client.hear_server();
			} catch (IOException e) {
				if (this.client.server_disconnected_right == false) {
					this.add_event(new GameError("Server connection failed."));
				}
				System.out.println("Lost connection to server...");
				System.exit(0);
			}
		}).start();
	}
	
	
	public void add_event(GameEvent event) {
		while(this.event_q.offer(event) == false) {}
	}
	
	private void process_event(GameEvent event) {
    	if (event instanceof PlayerRegistered pr) {
    		this.updatePlayers(pr);
    	} else if (event instanceof HandsDealt hd) {
    		this.setCards(hd);
    	} else if (event instanceof PlayerTurn pt) {
    		this.updatePlayerTurn(pt);
    	} else if (event instanceof CardPlayed cp) {
    		this.updateCardPlayed(cp);
    	} else if (event instanceof TrickTaken tt) {
    		this.onTrickTaken(tt);
    	} else if (event instanceof PlayerError pe) {
        	this.endDialog("Player Error", pe.msg()+" -> causes from "+Player.toString(pe.player()));
        } else if (event instanceof GameError ge) {
        	if (ge.msg().contains("failed") && ge.msg().contains("connection")) {
        		this.endDialog("Connection lost!", "Game Error: " + ge.msg());
        	} else {
        		this.endDialog("Game Error: " + ge.msg());
        	}
        } else if (event instanceof GameOver go) {
        	String message = "";
        	ArrayList<Player> score_list = Score.getScoreList(go.score());
        	for(Player p:score_list) {
        		if (go.score().score().get(p) >= go.score().score().get(score_list.get(0))) {
        			if(message.equals("") == false) {
        				message += ", ";
        			}
        			message += Player.toString(p);
        		}
        	}
        	message += "\nWINS\n\n";
        	message += Score.toString(go.score());
        	this.endDialog(message);
        }
	}

	// Gamelogic Methods
	@Override
	public void updatePlayers(PlayerRegistered pr) {
		
	}

	@Override
	public void setCards(HandsDealt hd) {
		
	}

	@Override
	public void updatePlayerTurn(PlayerTurn pt) {
		// wait
		try {
			Thread.sleep((int)(Math.random()*(5000-500)+500));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (this.client.getPlayer() == pt.player()) {
			Card card = null;
			
			// check if ai is first player
			if (this.client.getTrick().cards().size() <= 0) {
				// check if is it realy first turn
				if (this.client.getCards().contains(new Card(Suit.Clubs, new Rank(2)))) {
					card = new Card(Suit.Clubs, new Rank(2));
				} else {
					card = this.client.getCards().iterator().next();
				}
			} else {
				for (Card c:this.client.getCards()) {
					if (c.suit() == this.client.getTrick().cards().first().card().suit()) {
						card = c;
						break;
					}
				}
				if (card == null) {
					card = this.client.getCards().iterator().next();
				}
			}
			
			try {
				this.client.send_to_server("playcard "+Card.toString(card));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateCardPlayed(CardPlayed cp) {
		
	}

	@Override
	public void onTrickTaken(TrickTaken tt) {
		
	}

	@Override
	public void endDialog(String msg) {
		System.out.println("End message: "+msg);
		System.exit(0);
	}
	
	@Override
	public void endDialog(String over_msg, String msg) {
		System.out.println("Head: "+over_msg+"\nEnd message: "+msg);
		System.exit(0);
	}

	@Override
	public void dispose() {
		System.exit(0);
	}

	
	public static void main(String[] args) {
		String host = "localhost";
		int port = 1234;
		// generate random name out of name list -> adjektiv/verb + nomen -> mehrere listen mixen
		String name = "Bot_"+(int)(Math.random()*(99-1)+1);
		new DumbAI(host, port, name).run();
	}

}

