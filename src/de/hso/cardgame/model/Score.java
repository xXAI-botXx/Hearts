package de.hso.cardgame.model;

import java.util.ArrayList;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;
import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;

public record Score(Map<Player, Integer> score) {

	public static String toString(Score s) {
		//P1:-23,P2:-2
		String result = "";
		for (Pair<Player, Integer> pair: s.score()) {
			result += Player.toString(pair.component1())+":"+pair.component2()+",";
		}
		result = result.substring(0, result.length() - 1);
		
		return result;
	}
	
	public static Score fromString(String str) {
		Map<Player, Integer> score_set = Maps.of();
		for (String player_entry:str.split(",")) {
			Player p = Player.fromString(player_entry.split(":")[0]);
			Integer score = Integer.valueOf(player_entry.split(":")[1]);
			score_set = score_set.put(p, score);
		}
		return new Score(score_set);
	}
	
	public static ArrayList<Player> getScoreList(Score score) {
		ArrayList<Player> score_list = new ArrayList<Player>();
		ArrayList<Player> player_left = new ArrayList<Player>();
		player_left.add(Player.P1);
		player_left.add(Player.P2);
		player_left.add(Player.P3);
		player_left.add(Player.P4);
		for (int i = 0; i < 3; i++) {
			Player player_with_highest_score = player_left.get(0);
			for (Player p:player_left) {
				if (p != player_with_highest_score) {
					if (score.score().get(p)  > score.score().get(player_with_highest_score)) {
						player_with_highest_score = p;
					}
				}
			}
			score_list.add(player_with_highest_score);
			player_left.remove(player_left.indexOf(player_with_highest_score));
		}
		return score_list;
	}
	
	public static ArrayList<ArrayList<Player>> getPrices(Score score) {
		ArrayList<ArrayList<Player>> winer = new ArrayList<ArrayList<Player>>();
		
		ArrayList<Player> first = new ArrayList<Player>();
		winer.add(first);
		ArrayList<Player> second = new ArrayList<Player>();
		winer.add(second);
		ArrayList<Player> third = new ArrayList<Player>();
		winer.add(third);
		ArrayList<Player> fourth = new ArrayList<Player>();
		winer.add(fourth);
		
		int counter = 0;
		ArrayList<Player> player_left = new ArrayList<Player>();
		player_left.add(Player.P1);
		player_left.add(Player.P2);
		player_left.add(Player.P3);
		player_left.add(Player.P4);
		
		for (int i = 0; i < 4; i++) {
			if (player_left.size() < 1) {
				break;
			}
			Player player_with_highest_score = player_left.get(0);
			for (Player p:player_left) {
				if (p != player_with_highest_score) {
					if (score.score().get(p)  > score.score().get(player_with_highest_score)) {
						player_with_highest_score = p;
					}
				}
			}
			if (counter == 0) {
				first.add(player_with_highest_score);
			} else if (counter == 1) {
				second.add(player_with_highest_score);
			} else if (counter == 2) {
				third.add(player_with_highest_score);
			} else if (counter == 3) {
				fourth.add(player_with_highest_score);
			}
			player_left.remove(player_left.indexOf(player_with_highest_score));
			
			// get all other players with this score
			for (int j = 0; j < player_left.size(); j++) {
				Player p = player_left.get(j);
				if (p != player_with_highest_score) {
					if (score.score().get(p)  == score.score().get(player_with_highest_score)) {
						if (counter == 0) {
							first.add(p);
						} else if (counter == 1) {
							second.add(p);
						} else if (counter == 2) {
							third.add(p);
						} else if (counter == 3) {
							fourth.add(p);
						}
						player_left.remove(player_left.indexOf(p));
					}
				}
			}
			// increase price counter
			counter++;
		}
		return winer;
	}
	
}
