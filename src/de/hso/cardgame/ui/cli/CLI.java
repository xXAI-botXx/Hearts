package de.hso.cardgame.ui.cli;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.andrewoma.dexx.collection.Set;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameError;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerError;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;
import de.hso.cardgame.ui.Client;
import de.hso.cardgame.ui.UI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class CLI implements UI {
	private Socket socket;
	private String name;
	private Scanner in;
	private Client client;
	private Queue event_q;
	private boolean should_run;

	public CLI() throws UnknownHostException, IOException {
		this.in = new Scanner(System.in);
		System.out.print("Port: ");
		int port = in.nextInt();
		String host = "localhost";
		//System.out.print("Host: ");
		//String host = in.nextLine();
		System.out.print("Name: ");
		this.name = in.next();
		System.out.println("Connecting to " + host + "...");
		
		this.client = new Client(host, port);
		
		this.event_q = new ConcurrentLinkedQueue<GameEvent>();
		this.should_run = true;
	}
	
	public void set_ui() {
		this.client.setUI(this);
	}
	
	public void register() {
		try {
			this.client.register(this.name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start_hearing() {
		new Thread(() -> {
			try {
				client.hear_server();
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("Lost connection to server...");
				System.exit(0);
			}
		}).start();
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
        CLI ui = new CLI();
        ui.set_ui();
        
        ui.start_hearing();
        
        ui.start_event_listening();
        
        ui.register();
        
        ui.run();
	}
	
	public void run() throws IOException {
		String cardString;
        do {
        	// FIX Input
        	cardString = in.nextLine();
        	System.out.println("Input: '"+cardString+"'");
        	if (cardString.length() > 0) {
	        	Card card = Card.fromString(cardString);
	        	if (card != null) {
	        		if (this.client.handContains(card)) {
	        			// TODO send message via socket
	        			client.send_to_server("playcard "+cardString);
	        			//cards.remove(card);
	        			//printCards();
	        		} else {
	        			System.err.println(cardString + " is not in your hand.");
	        		}
	        	}
        	}
        } while (!cardString.toLowerCase().equals("exit"));
        
        in.close();
	}
	
	@Override
	public void dispose() {}

	@Override
	public void updatePlayers(PlayerRegistered pr) {
		if (this.client.getPlayer() == pr.player()) {
			System.out.println("You are Player " + this.client.getPlayer());
			System.out.print("Other players: ");
			
			for (var p: pr.otherPlayers()) {
				System.out.print(p.component2() + " (" + p.component1() + "), ");
			}
		} else {
			System.out.println(pr.player().toString() + " joined the game");
		}
		
	}

	@Override
	public void setCards(HandsDealt hd) {
		printCards();
	}

	@Override
	public void updatePlayerTurn(PlayerTurn pt) {
		System.out.println("Player Turn: " + pt.player().toString());
	}

	@Override
	public void updateCardPlayed(CardPlayed cp) {
		if (cp.player() == this.client.getPlayer()) {
			System.out.println("You played " + cp.card().toString());
		} else {
			System.out.println(cp.player().toString() + " played " + cp.card().toString());
		}
	}

	@Override
	public void onTrickTaken(TrickTaken tt) {
		if (tt.player() == this.client.getPlayer()) {
			System.out.println("You takes the trick.\n    -> New Points:"+tt.points());
		} else {
			System.out.println(tt.player() + " takes the trick.\n    -> New Points:"+tt.points());
		}
	}

	@Override
	public void endDialog(String string) {
		this.should_run = false;
		System.out.println(string);
		System.exit(0);
	}
	
	private void printCards() {
		System.out.println("Your cards:");
		for (var card : this.client.getCards()) {
			System.out.println(card.toString());
		}
	}

	@Override
	public void add_event(GameEvent event) {
		while(this.event_q.offer(event) == false) {}
	}

	@Override
	public void endDialog(String over_msg, String msg) {
		this.should_run = false;
	}
	
	private void start_event_listening() {
		new Thread(() -> {
			while (this.should_run) {
				if (event_q.isEmpty() == false) 
					process_event((GameEvent) event_q.poll());	
			}
		}).start();
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
        	this.endDialog("Player Error: "+pe.msg()+" -> causes from "+Player.toString(pe.player()));
        } else if (event instanceof GameError ge) {
        	if (ge.msg().contains("failed") && ge.msg().contains("connection")) {
        		this.endDialog("Connection lost!  Game Error: " + ge.msg());
        	} else {
        		this.endDialog("Game Error: " + ge.msg());
        	}
        } else if (event instanceof GameOver go) {
        	String message = "";
        	//ArrayList<Player> score_list = Score.getScoreList(go.score());
        	ArrayList<ArrayList<Player>> prices = Score.getPrices(go.score());
        	int counter = 0;
        	for(ArrayList<Player> price:prices) {
        		for (Player p:price) {
        			if (counter == 0) {
        				message += "\n1. "+Player.toString(p) + " (" +go.score().score().get(p) +")";
        			} else if (counter == 1) {
        				message += "\n2. "+Player.toString(p) + " (" +go.score().score().get(p) +")";
        			} else if (counter == 2) {
        				message += "\n3. "+Player.toString(p) + " (" +go.score().score().get(p) +")";
        			} else if (counter == 3) {
        				message += "\n4. "+Player.toString(p) + " (" +go.score().score().get(p) +")";
        			}
        		}
        		counter++;
        	}
        	//message += "\nWINS\n\n";
        	//message += Score.toString(go.score());
        	this.endDialog(message);
        }
	}
}
