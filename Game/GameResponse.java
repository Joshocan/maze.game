package Game;

import java.io.Serializable;

import Tracker.PlayerList;

public class GameResponse implements Serializable {
	private static final long serialVersionUID = -2415034804425263861L;

	private Boolean ack = false;
	
	private GameState gameState;
	
	private PlayerList playerList;
	
	public Boolean getAck() {
		return ack;
	}

	public void setAck(Boolean ack) {
		this.ack = ack;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public PlayerList getPlayerList() {
		return playerList;
	}

	public void setPlayerList(PlayerList playerList) {
		this.playerList = playerList;
	}

	@Override
	public String toString() {
		return "GameResponse [ack=" + ack + ", gameState=" + gameState + ", playerList=" + playerList + "]";
	}
}
