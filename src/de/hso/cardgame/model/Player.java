package de.hso.cardgame.model;

public enum Player {
	P1, P2, P3, P4;
	
	public static String toString(Player p) {
		if (p == Player.P1) {
			return "P1";
		} else if (p == Player.P2) {
			return "P2";
		} else if (p == Player.P3) {
			return "P3";
		} else if (p == Player.P4) {
			return "P4";
		} else {
			return null;
		}
	}
	
	public static Player fromString(String str) {
		if (str.equals("P1")) {
			return Player.P1;
		} else if (str.equals("P2")) {
			return Player.P2;
		} else if (str.equals("P3")) {
			return Player.P3;
		} else if (str.equals("P4")) {
			return Player.P4;
		} else {
			return null;
		}
	}
	
	public static Player index_to_player(int index) {
		if (index == 0) {
			return Player.P1;
		} else if (index == 1) {
			return Player.P2;
		} else if (index == 2) {
			return Player.P3;
		} else {
			return Player.P4;
		}
	}
	
	public static int player_to_index(Player player) {
		if (player == Player.P1) {
			return 0;
		} else if (player == Player.P2) {
			return 1;
		} else if (player == Player.P3) {
			return 2;
		} else {
			return 3;
		}
	}
	
	public static String[] next_players_as_str(Player player) {
		if (player == Player.P1) {
			return new String[]{"P2", "P3", "P4"};
		} else if (player == Player.P2) {
			return new String[]{"P3", "P4", "P1"};
		} else if (player == Player.P3) {
			return new String[]{"P4", "P1", "P2"};
		} else {
			return new String[]{"P1", "P2", "P3"};
		}
	}
}


