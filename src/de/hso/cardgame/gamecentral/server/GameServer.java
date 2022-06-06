package de.hso.cardgame.gamecentral.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameError;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerError;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Suit;


/**
 * Server
 */
public class GameServer implements EventConsumer{
	private int port;
	private ArrayList<ClientHandler> clients;
	private GameLogic game;
	private GameStatus status;
	
	public GameServer (int port) {
		this.port = port;
		this.clients = new ArrayList<ClientHandler>();
		this.game = new GameLogic(GameState.empty, this);
		this.status = GameStatus.REGISTRATION;
	}
	
	public void run() throws IOException {
		 try (ServerSocket serverSocket = new ServerSocket(this.port)) {
		        System.out.println("Waiting for clients on port " + this.port);
		    while (true) {
		    	if (this.status == GameStatus.REGISTRATION) {
			        Socket sock = serverSocket.accept();
			        if (this.status == GameStatus.REGISTRATION) {
				        System.out.println("Got new connection: " + sock);
				        ClientHandler handler = new ClientHandler(sock, this);
				        handler.start();
			        }
		    	} else if (this.status == GameStatus.PLAYING) {
		    		
		    	} else if (this.status == GameStatus.GAMEOVER) {
		    		
		    	}
		    }
		}
	}

	/**
	 * Sending Events to all Clients
	 */
	@Override
	public void consumeEvent(GameEvent event) {
		// if 4 players Registered => DealHandsCommand to GameLogic 
		if (event.getClass() == PlayerRegistered.class) {
			//PlayerRegistered real_event = (PlayerRegistered) event;
			// send to own Player
			//int index_of_player = Player.player_to_index(real_event.player());
			//String msg_to_new_player = GameEventSerializer.toJSON(event)+"#you";
			//this.send_to_clients(new int[]{index_of_player}, msg_to_new_player);
			
			// send to others
			String msg = GameEventSerializer.toString(event);
			this.send_to_all_clients(msg);
			
		} else if (event.getClass() == HandsDealt.class) {
			HandsDealt real_event = (HandsDealt) event;
			Player p = real_event.player();
			// only send to the one player
			this.send_to_clients(new int[]{Player.player_to_index(p)}, GameEventSerializer.toString(real_event));
		} else if (event.getClass() == PlayerTurn.class) {
			PlayerTurn real_event = (PlayerTurn) event;
			Player p = real_event.player();
			// only send to the one player
			this.send_to_clients(new int[]{Player.player_to_index(p)}, GameEventSerializer.toString(real_event));
		} else if (event.getClass() == CardPlayed.class) {
			String msg = GameEventSerializer.toString(event);
			this.send_to_all_clients(msg);
		} else if (event.getClass() == TrickTaken.class) {
			String msg = GameEventSerializer.toString(event);
			this.send_to_all_clients(msg);
		} else if (event.getClass() == GameError.class) {
			if (this.status != GameStatus.GAMEOVER) {
				this.status = GameStatus.CANCELD;
				String msg = GameEventSerializer.toString(event);
				this.send_to_all_clients(msg);
				// close the server
				System.exit(0);
			}
		} else if (event.getClass() == PlayerError.class) {
			if (this.status != GameStatus.GAMEOVER) {
				PlayerError real_event = (PlayerError) event;
				Player p = real_event.player();
				this.status = GameStatus.CANCELD;
				String msg = GameEventSerializer.toString(event);
				this.send_to_all_clients_except(msg, p);
				// close the server
				System.exit(0);
			}
		} else if (event.getClass() == GameOver.class) {
			this.status = GameStatus.GAMEOVER;
			String msg = GameEventSerializer.toString(event);
			this.send_to_all_clients(msg);
			// close the server
			System.exit(0);
		} 
	}
	
	private void send_to_clients(int[] indexes, String message) {
		// check where to send the event
		for(int i:indexes) {
			try {
				this.clients.get(i).send_to_client(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void send_to_all_clients(String message) {
		// check where to send the event
		for(ClientHandler handler:this.clients) {
			try {
				handler.send_to_client(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void send_to_all_clients_except(String message, Player except) {
		// check where to send the event
		for (int i = 0; i< this.clients.size(); i++) {
			try {
				if (Player.index_to_player(i) != except)
					this.clients.get(i).send_to_client(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adding Client/Player -> index => Player Type
	 */
	public void add_player(ClientHandler handler, String name) {
		if (this.clients.size() < 4) {
			this.clients.add(handler);
		}
	}
	
	/**
	 * Process the command from Client
	 */
	public void processCommand(ClientHandler handler, String command) {
		if (command.split(" ")[0].equalsIgnoreCase("register") && this.status == GameStatus.REGISTRATION) {
        	String name = command.split(" ")[1];
        	this.add_player(handler, name);
        	RegisterPlayer cmd = new RegisterPlayer(Player.index_to_player(this.clients.indexOf(handler)), name);
        	this.game.processCommand(cmd);
        	
        	// post process
        	if (this.clients.size() == 4) {
        		this.status = GameStatus.PLAYING;
        		System.out.println("All Player are registered.\n\n##############################\n### Let's start the Games ###\n##############################\n");
        		this.game.processCommand(new DealHands(this.game.getPlayerHands()));
        	}
        } else if (command.split(" ")[0].equalsIgnoreCase("playcard") && this.status == GameStatus.PLAYING) {
        	String card_str = command.split(" ")[1];
        	Card card = Card.fromString(card_str);
        	// check, if input is a card
        	if (card != null) {
        		PlayCard cmd = new PlayCard(Player.index_to_player(this.clients.indexOf(handler)), card);
	        	this.game.processCommand(cmd);
        	} 
        }
	}
		
	public void disconnect(ClientHandler handler) {
		if (this.status == GameStatus.REGISTRATION) {
			System.out.println("An Player has exited the game. Game breaks.");
			PlayerError error = new PlayerError(Player.index_to_player(this.clients.indexOf(handler)), "Leaved the game...");
			this.send_to_all_clients_except(GameEventSerializer.toString(error), error.player());
			this.status = GameStatus.CANCELD;
			System.exit(0);
		} else if (this.status == GameStatus.PLAYING) {
			this.status = GameStatus.CANCELD;
			PlayerError error = new PlayerError(Player.index_to_player(this.clients.indexOf(handler)), "Leaved the game...");
			this.send_to_all_clients_except(GameEventSerializer.toString(error), error.player());
			System.exit(0);
		}
	}
	
	/**
	 * Start Server
	 * -> Looking for Clients
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Port: ");
		Scanner in = new Scanner(System.in);
		int port = in.nextInt();
		in.close();
		GameServer server = new GameServer(port);
		server.run();
	}
}


/**
 * Class for Client-Management -> get communication from client to server
 */
class ClientHandler extends Thread {
	private Socket socket;
	private GameServer server;
	private InputStreamReader reader;
	private OutputStreamWriter writer;
	private BufferedReader buffered_reader;
	private BufferedWriter buffered_writer;
	
	public ClientHandler(Socket socket, GameServer server) throws IOException {
		this.socket = socket;	
		this.server = server;
		//this.events = new ConcurrentLinkedQueue<String>();
		
		this.reader = new InputStreamReader(this.socket.getInputStream());
		this.writer = new OutputStreamWriter(this.socket.getOutputStream());
		
		this.buffered_reader = new BufferedReader(this.reader);
		this.buffered_writer = new BufferedWriter(this.writer);
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
	
	/**
	 * Starts the Client in a Thread
	 */
	public void run() {
        try {
            this.hear_client();
        } catch (IOException e) {
            log("terminated: " + e.getMessage());
            this.server.disconnect(this);
        }
    }
	
	/**
	 * helper method for a good error-message
	 */
	private void log(String s) {
        LocalDateTime now = LocalDateTime.now();
        String t = now.format(DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("[" + t + " client " + this.socket.getPort() + "] " + s);
    }
	
	/**
	 * channel from client to server
	 */
	private void hear_client() throws IOException {
        String line = this.buffered_reader.readLine();  // get first request from client
        while (line != null) {
            log("Got line from client: " + line);
            line = line.strip();
            this.server.processCommand(this, line); // compute answer
            line = this.buffered_reader.readLine();         // get next request from client
        }
    }
	
	/**
	 * channel from server to client
	 */
	public void send_to_client(String message) throws IOException {
		log("Send to client: " + message);
		this.buffered_writer.write(message+"\n");
		this.buffered_writer.flush();
    }  

}
