package de.hso.cardgame.ui.gui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;

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
import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.Suit;
import de.hso.cardgame.model.Trick;
import de.hso.cardgame.ui.Client;
import de.hso.cardgame.ui.UI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class GUI extends Application implements UI{
	// constants
	private static final int WIDTH=1400, HEIGHT=700;
	private static final int MIN_WIDTH=1400, MIN_HEIGHT=700;
	private static final String PATH="C:\\Users\\tobia\\Karriere\\Studium\\3. Semester\\Module\\Programmieren_mit_Java\\Praktikum\\Abschlussprojekt\\cardgame-xXAI-botXx\\res\\img";
	
	// Gamelogic
	private	Client client;
	private Map<String, Button> trick;
	private Map<String, Button> hand;
	private Queue event_q;
	
	// Scenes
	private Scene connection_scene;
	private Scene play_scene;
	
	// connection screen
	private VBox connection_root_pane;
	private Label label_title;
	private TextField txt_host;
	private TextField txt_port;
	private TextField txt_name;
	private Button btn_connect;
	
	// play scene
	private BorderPane play_root_pane;
	private HBox hbox_trick;
	private HBox hbox_hand;
	private VBox vbox_self_player;
	private VBox vbox_left_player;
	private VBox vbox_right_player;
	private VBox vbox_top_player;
	private Label label_points_self;
	private Label label_points_left;
	private Label label_points_right;
	private Label label_points_top;
	private Label label_name_self;
	private Label label_name_left;
	private Label label_name_right;
	private Label label_name_top;
	
	private VBox vbox_self;
	
	public GUI() {
		this.trick = Maps.of();
		this.hand = Maps.of();
		this.event_q = new ConcurrentLinkedQueue<GameEvent>();
	}
	
	@Override
	public void start(Stage primary_stage) throws Exception {
		// create both scenes
		this.init_connection_scene(primary_stage);
		this.init_play_scene(primary_stage);
		
		
		// start with connection scene
		//primary_stage.initStyle(StageStyle.TRANSPARENT);
		primary_stage.setScene(this.connection_scene);
		primary_stage.setTitle("Hearts <3");
		//primary_stage.setResizable(false);
		primary_stage.setMinHeight(GUI.MIN_HEIGHT);
		primary_stage.setMinWidth(GUI.MIN_WIDTH);
		primary_stage.show();
		
		primary_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent e) {
		    	if (client != null)
		    		client.exit();
		    	Platform.exit();
		    	System.exit(0);
		    }
		  });
		
		this.start_event_listening();
	}
	
	private void init_connection_scene(Stage primary_stage) {
		// elements
		this.label_title = new Label("Hearts <3");
		this.label_title.setFont(new Font("Helvetica", 48));
		this.label_title.setTextFill(Color.RED);
		
		this.txt_host = new TextField("localhost");
		this.txt_port = new TextField("1234");
		this.txt_name = new TextField("anonymous");
		
		this.btn_connect = new Button("Connect");
		this.btn_connect.setScaleX(1.5);
		this.btn_connect.setScaleY(1.5);
		this.btn_connect.setOnMouseClicked(new EventHandler<MouseEvent>() {	
			@Override
			public void handle(MouseEvent arg0) {
				boolean connected = connect_to_server(txt_host.getText(), txt_port.getText(), txt_name.getText());
				if (connected) {
					primary_stage.setScene(play_scene);
					primary_stage.show();
				}
			}
		});
		
		
		// layout
		this.connection_root_pane = new VBox(this.label_title, this.txt_host, this.txt_port, this.txt_name, this.btn_connect);
		this.connection_root_pane.setSpacing(20);
		this.connection_root_pane.setAlignment(Pos.CENTER);
		this.connection_root_pane.setMargin(this.label_title, new Insets(20, 0, 80, 0));
		this.connection_root_pane.setMargin(this.txt_name, new Insets(0, 300, 0, 300));
		this.connection_root_pane.setMargin(this.txt_port, new Insets(0, 300, 0, 300));
		this.connection_root_pane.setMargin(this.txt_host, new Insets(0, 300, 0, 300));
		
		// create scene
		this.connection_scene = new Scene(this.connection_root_pane, GUI.WIDTH, GUI.HEIGHT);    //, Color.TRANSPARENT
		// set focus
		this.btn_connect.requestFocus();
	}
	
	private void init_play_scene(Stage primary_stage) {
		// layout
		this.play_root_pane = new BorderPane();
		this.play_root_pane.setMinSize(600, 400);
		this.play_root_pane.setPadding(new Insets(20, 20, 20, 20));
		
		// elements
		this.label_name_self = new Label("Unknown");
		this.label_name_self.setPadding(new Insets(2,2,2,2));
		this.label_points_self = new Label("Points: 0");
		this.vbox_self_player = new VBox(this.label_name_self, this.label_points_self);
		this.vbox_self_player.setAlignment(Pos.CENTER);
		
		this.label_name_left = new Label("Unknown");
		this.label_name_left.setPadding(new Insets(2,2,2,2));
		this.label_points_left = new Label("Points: 0");
		this.vbox_left_player = new VBox(this.label_name_left, this.label_points_left);
		this.vbox_left_player.setAlignment(Pos.CENTER);
		
		this.label_name_top = new Label("Unknown");
		this.label_name_top.setPadding(new Insets(2,2,2,2));
		this.label_points_top = new Label("Points: 0");
		this.vbox_top_player = new VBox(this.label_name_top, this.label_points_top);
		this.vbox_top_player.setAlignment(Pos.CENTER);
		
		this.label_name_right = new Label("Unknown");
		this.label_name_right.setPadding(new Insets(2,2,2,2));
		this.label_points_right = new Label("Points: 0");
		this.vbox_right_player = new VBox(this.label_name_right, this.label_points_right);
		this.vbox_right_player.setAlignment(Pos.CENTER);
		
			// Trick
		this.hbox_trick = new HBox();
		this.hbox_trick.setMinHeight(200);
		this.hbox_trick.setAlignment(Pos.CENTER);
		this.hbox_trick.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 5;"
		        + "-fx-border-radius: 5;" + "-fx-border-color: black;");
		
			// Hand
		this.hbox_hand = new HBox();
		this.hbox_hand.setMinHeight(200);
		this.hbox_hand.setAlignment(Pos.CENTER);
		this.hbox_hand.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;"
			        + "-fx-border-width: 1;" + "-fx-border-insets: 5;"
			        + "-fx-border-radius: 5;" + "-fx-border-color: black;");
		
		this.vbox_self = new VBox(this.vbox_self_player, this.hbox_hand);
		this.vbox_self.setAlignment(Pos.CENTER);
		
		// add to root pane
		this.play_root_pane.setTop(this.vbox_top_player);
		this.play_root_pane.setLeft(this.vbox_left_player);
		this.play_root_pane.setRight(this.vbox_right_player);
		this.play_root_pane.setCenter(this.hbox_trick);
		this.play_root_pane.setBottom(this.vbox_self);
		
		// spacing
		this.play_root_pane.setMargin(this.hbox_trick, new Insets(50, 20, 50, 20));
		//this.play_root_pane.setPadding(new Insets(50, 20, 50, 20));

		
		// create scene
		this.play_scene = new Scene(this.play_root_pane, GUI.WIDTH, GUI.HEIGHT);
		// set focus
		this.btn_connect.requestFocus();
	}
	
	private void add_card_to_trick(String card) {
		Image image = new Image(GUI.PATH+"\\"+card+".png", 90, 120, true, true);
		Button new_card_btn = new Button();
		new_card_btn.setBackground(null);
		new_card_btn.setGraphic(new ImageView(image));
		this.trick = this.trick.put(card, new_card_btn);
		this.hbox_trick.getChildren().add(new_card_btn);
	}
	
	private void clear_trick() {
		this.trick = Maps.of();
		this.hbox_trick.getChildren().clear();
	}
	
	private void add_card_to_hand(String card) {
		Image image = new Image(GUI.PATH+"\\"+card+".png", 90, 120, true, true);
		Button new_card_btn = new Button();
		new_card_btn.setBackground(null);
		new_card_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {	
			@Override
			public void handle(MouseEvent arg0) {
				try {
					client.send_to_server("playcard "+card+"#");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		new_card_btn.setGraphic(new ImageView(image));
		this.hand  = this.hand.put(card, new_card_btn);
		this.hbox_hand.getChildren().add(new_card_btn);
	}
	
	private void remove_card_from_hand(String card) {
		Button btn = this.hand.get(card);
		this.hand = this.hand.remove(card);
		this.hbox_hand.getChildren().remove(btn);
	}
	
	private boolean connect_to_server(String host, String port, String name) {
		try {
			this.client = new Client(host, Integer.parseInt(port));
			this.set_ui();
	        this.start_hearing();
	        this.register();
	        this.client.setName(name);
	        System.out.println("Client runs now on: "+port);
			return true;
		} catch (NumberFormatException e) {
			System.out.println("Error by parsing port to int.");
			return false;
		} catch (IOException e) {
			System.out.println("No server on this port found...");
			return false;
		}
	}
	
	private void set_ui() {
		this.client.setUI(this);
	}
	
	private void register() {
		try {
			this.client.register(this.txt_name.getText());
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
				//e.printStackTrace();
				System.out.println("Lost connection to server...");
				//System.exit(0);
			}
		}).start();
	}
	
	private void register_player(Player self, Player another, String name) {
		if (self == Player.P1) {
			if (another == Player.P2) {
				this.label_name_left.setText(""+name+" ("+another+")");
			} else if (another == Player.P3) {
				this.label_name_top.setText(""+name+" ("+another+")");
			} if (another == Player.P4) {
				this.label_name_right.setText(""+name+" ("+another+")");
			}
		} else if (self == Player.P2) {
			if (another == Player.P3) {
				this.label_name_left.setText(""+name+" ("+another+")");
			} else if (another == Player.P4) {
				this.label_name_top.setText(""+name+" ("+another+")");
			} if (another == Player.P1) {
				this.label_name_right.setText(""+name+" ("+another+")");
			}
		} else if (self == Player.P3) {
			if (another == Player.P4) {
				this.label_name_left.setText(""+name+" ("+another+")");
			} else if (another == Player.P1) {
				this.label_name_top.setText(""+name+" ("+another+")");
			} if (another == Player.P2) {
				this.label_name_right.setText(""+name+" ("+another+")");
			}
		} else if (self == Player.P4) {
			if (another == Player.P1) {
				this.label_name_left.setText(""+name+" ("+another+")");
			} else if (another == Player.P2) {
				this.label_name_top.setText(""+name+" ("+another+")");
			} if (another == Player.P3) {
				this.label_name_right.setText(""+name+" ("+another+")");
			}
		}
	}
	
	private void start_event_listening() {
		Timeline event_listener = new Timeline(
                new KeyFrame(Duration.millis(5), 
                new EventHandler<ActionEvent>() {

		   @Override
		   public void handle(ActionEvent event) {
		       if (event_q.isEmpty() == false) {
		    	   process_event((GameEvent) event_q.poll());
		       }
		   }
		}));
		event_listener.setCycleCount(Timeline.INDEFINITE);
		event_listener.play();
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
        		this.endDialog("Game Error: ", ge.msg());
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
	
	
	// Gamelogic Methods
	@Override
	public void updatePlayers(PlayerRegistered pr) {
		if (this.client.getPlayer() == pr.player()) {
			this.label_name_self.setText(""+this.client.getName()+" ("+Player.toString(this.client.getPlayer())+")");
			
			for (var p: pr.otherPlayers()) {
				this.register_player(this.client.getPlayer(), p.component1(), p.component2());
			}
		} else {
			this.register_player(this.client.getPlayer(), pr.player(), pr.name());
		}
	}

	@Override
	public void setCards(HandsDealt hd) {
		for (Card c:hd.hand().cards()) {
			String card_str = Card.toString(c);
			card_str = card_str.replaceAll("#", "");
			// load and set image
			this.add_card_to_hand(card_str);
		}
	}

	@Override
	public void updatePlayerTurn(PlayerTurn pt) {
		if (this.client.getPlayer() == pt.player()) {
			this.label_name_self.setBorder(new Border(new BorderStroke(Color.INDIANRED, BorderStrokeStyle.SOLID, new CornerRadii(1.0), BorderStroke.THICK)));
		}
	}

	@Override
	public void updateCardPlayed(CardPlayed cp) {
		String card_str = Card.toString(cp.card());
		card_str = card_str.replaceAll("#", "");
		if (this.client.getPlayer() == cp.player()) {
			this.remove_card_from_hand(card_str);
			this.label_name_self.setBorder(null);
		} 
		this.add_card_to_trick(card_str);
	}

	@Override
	public void onTrickTaken(TrickTaken tt) {
		this.clear_trick();
		Player self = this.client.getPlayer();
		Player another = tt.player();
		
		if (self == another) {
			this.label_points_self.setText("Points: "+tt.points());
		}
		
		if (self == Player.P1) {
			if (another == Player.P2) {
				this.label_points_left.setText("Points: "+tt.points());
			} else if (another == Player.P3) {
				this.label_points_top.setText("Points: "+tt.points());
			} else if (another == Player.P4) {
				this.label_points_right.setText("Points: "+tt.points());
			}
		} else if (self == Player.P2) {
			if (another == Player.P3) {
				this.label_points_left.setText("Points: "+tt.points());
			} else if (another == Player.P4) {
				this.label_points_top.setText("Points: "+tt.points());
			} else if (another == Player.P1) {
				this.label_points_right.setText("Points: "+tt.points());
			}
		} else if (self == Player.P3) {
			if (another == Player.P4) {
				this.label_points_left.setText("Points: "+tt.points());
			} else if (another == Player.P1) {
				this.label_points_top.setText("Points: "+tt.points());
			} if (another == Player.P2) {
				this.label_points_right.setText("Points: "+tt.points());
			}
		} else if (self == Player.P4) {
			if (another == Player.P1) {
				this.label_points_left.setText("Points: "+tt.points());
			} else if (another == Player.P2) {
				this.label_points_top.setText("Points: "+tt.points());
			} else if (another == Player.P3) {
				this.label_points_right.setText("Points: "+tt.points());
			}
		}
	}

	@Override
	public void endDialog(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Hearts <3 - Important Message");
		alert.setHeaderText("The Game is Over");
		alert.setContentText(msg);
		alert.show();
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				if(client != null)
					client.exit();
				Platform.exit();
		    	System.exit(0);
			}
		});
		//Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        //alert.setX(bounds.getMaxX() - GUI.WIDTH/2);
        //alert.setY(bounds.getMaxY() - GUI.HEIGHT/2);
		Scene parent = this.play_scene;
		BorderPane root = this.play_root_pane;
		double x = parent.getX() + (parent.getWidth() / 2) - root.getPrefWidth() / 2;
        double y = parent.getY() + (parent.getHeight() / 2)- root.getPrefHeight() / 2;
        alert.setX(x);
        alert.setY(y);
	}
	
	@Override
	public void endDialog(String over_msg, String msg) {
		BoxBlur blur_fx = new BoxBlur(3,3,3);
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Hearts <3 - Important Message");
		alert.setHeaderText(over_msg);
		alert.setContentText(msg);
		this.play_root_pane.setEffect(blur_fx);
		alert.show();
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				play_root_pane.setEffect(null);
				if(client != null)
					client.exit();
				Platform.exit();
		    	System.exit(0);
			}
		});
		Scene parent = this.play_scene;
		BorderPane root = this.play_root_pane;
		double x = parent.getX() + (parent.getWidth() / 2) - root.getPrefWidth() / 2;
        double y = parent.getY() + (parent.getHeight() / 2)- root.getPrefHeight() / 2;
        alert.setX(x);
        alert.setY(y);
        alert.setResizable(false);
	}

	@Override
	public void dispose() {
		//Platform.exit();
    	//System.exit(0);
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
