package de.hso.cardgame.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.github.andrewoma.dexx.collection.Set;

import de.hso.cardgame.gamecentral.server.EventConsumer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
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
import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Trick;

public class Client {
	private Socket socket;
	private InputStreamReader reader;
	private OutputStreamWriter writer;
	private BufferedReader buffered_reader;
	private BufferedWriter buffered_writer;
	private Player player;
	private Trick curTrick;
	private Set<Card> cards;
	private String name;
	private UI ui;
	private boolean should_run;
	public boolean server_disconnected_right;
	private boolean game_over;
	
	public Client(String host, int port) throws UnknownHostException, IOException {
		this.socket = new Socket(host, port);
		
		this.reader = new InputStreamReader(this.socket.getInputStream());
		this.writer = new OutputStreamWriter(this.socket.getOutputStream());
		
		this.buffered_reader = new BufferedReader(this.reader);
		this.buffered_writer = new BufferedWriter(this.writer);
		
		this.should_run = true;
		this.server_disconnected_right = false;
		this.game_over = false;
		
		this.curTrick = Trick.empty;
	}
	
	 protected void finalize() {
	     try {
			if (this.buffered_reader != null) this.buffered_reader.close();
			if (this.buffered_writer != null) this.buffered_writer.close();
			if (this.reader != null) this.reader.close();
			if (this.writer != null) this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }
	
	public void register(String name) throws IOException {
		// TODO register client with given name
		this.name = name;
		this.send_to_server("register "+name);
	}
	
	public void hear_server() throws IOException {
		this.game_over = false;
		
		while (!this.game_over && this.should_run) {
			// get Server command
        	var message = this.buffered_reader.readLine();
        	System.out.println("\nGot Event: '"+message+"'");
        	var event = GameEventSerializer.fromString(message.replace("\n", ""));
        	
        	
        	// react to it
        	if (event instanceof PlayerRegistered pr) {
        		if (this.player == null) 
        			this.player = pr.player();
        		//ui.updatePlayers(pr);
        	} else if (event instanceof HandsDealt hd) {
        		this.cards = hd.hand().cards();
        		//ui.setCards(hd);
        	} else if (event instanceof PlayerTurn pt) {
        		//ui.updatePlayerTurn(pt);
        	} else if (event instanceof CardPlayed cp) {
        		this.curTrick = new Trick(this.curTrick.cards().append(new PlayerCard(cp.player(), cp.card())));
        		this.cards = this.cards.remove(cp.card());
        		//ui.updateCardPlayed(cp);
        	} else if (event instanceof TrickTaken tt) {
        		this.curTrick = Trick.empty;
        		//ui.onTrickTaken(tt);
        	} else if (event instanceof PlayerError pe) {
        		this.server_disconnected_right = true;
        		this.game_over = true;
        		game_over = true;
        		//ui.endDialog("Player Error (" + pe.player() + "): " + pe.msg());
	        } else if (event instanceof GameError ge) {
	        	this.server_disconnected_right = true;
	        	this.game_over = true;
	        	game_over = true;
	        	//ui.endDialog("Game Error: " + ge.msg());
	        } else if (event instanceof GameOver go) {
	        	this.server_disconnected_right = true;
	        	this.game_over = true;
	        	game_over = true;
	        	//ui.endDialog("Game Over: " + go.score());
	        }
        	
        	ui.add_event(event);
        }
		
		ui.dispose();
	}
		
	public Player getPlayer() {
		return this.player;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<Card> getCards() {
		return this.cards;
	}
	
	public boolean getGameover() {
		return this.game_over;
	}
	
	public Trick getTrick() {
		return this.curTrick;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setGameover(boolean bool) {
		this.game_over = bool;
	}
	
	public void send_to_server(String message) throws IOException {
		this.buffered_writer.write(message+"\n");
		this.buffered_writer.flush();
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setUI(UI ui) {
		this.ui = ui;
	}
	
	public boolean handContains(Card c) {
		return this.cards.contains(c);
	}
	
	public void exit() {
		this.should_run = false;
		System.exit(0);
	}
	
}
