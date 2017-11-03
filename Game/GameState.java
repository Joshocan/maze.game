package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = -3116298325561034676L;
	Map<String, Integer> playerScoresMap = new HashMap<>();
	Map<String, Position> playerPositionsMap = new HashMap<>();
	List<Position> treasures = new ArrayList<>();
	
	public Map<String, Integer> getPlayerScoresMap() {
		return playerScoresMap;
	}
	public void setPlayerScoresMap(Map<String, Integer> playerScoresMap) {
		this.playerScoresMap = playerScoresMap;
	}
	public Map<String, Position> getPlayerPositionsMap() {
		return playerPositionsMap;
	}
	public void setPlayerPositionsMap(Map<String, Position> playerPositionsMap) {
		this.playerPositionsMap = playerPositionsMap;
	}
	public List<Position> getTreasures() {
		return treasures;
	}
	public void setTreasures(List<Position> treasures) {
		this.treasures = treasures;
	}
	
	public void removePlayerById(String playerId) {
		playerScoresMap.remove(playerId);
		playerPositionsMap.remove(playerId);
	}
	
	@Override
	public String toString() {
		return "GameState [playerScoresMap=" + playerScoresMap + ", playerPositionsMap=" + playerPositionsMap
				+ ", treasures=" + treasures + "]";
	}

}
