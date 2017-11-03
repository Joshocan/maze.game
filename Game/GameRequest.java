package Game;

import java.io.Serializable;

import Tracker.PlayerInfo;
import Tracker.PlayerList;

public class GameRequest implements Serializable {
	private static final long serialVersionUID = 8290595191743952325L;
	public enum GameRequestType {
		HEALTH_CHECK_PING(-5), BACKUP_DEAD(-4), PRIMARY_DEAD(-3),SYNC_BACKUP(-2), JOIN(-1), REFRESH(0),WEST(1), SOUTH(2), EAST(3), NORTH(4), QUIT(9);
		private int type;
		private GameRequestType(int type) {
			this.type = type;
		}
		private int getType() {
			return type;
		}
	}
	
	private GameRequestType gameRequestType;
	private String playId;
	private PlayerInfo playerInfo;
	private GameState gameState;
	private PlayerList playerList;

	
	public GameRequest(GameRequestType gameRequestType, String playId) {
		super();
		this.gameRequestType = gameRequestType;
		this.playId = playId;
	}
	public GameRequestType getGameRequestType() {
		return gameRequestType;
	}
	public void setGameRequestType(GameRequestType gameRequestType) {
		this.gameRequestType = gameRequestType;
	}
	public String getPlayId() {
		return playId;
	}
	public void setPlayId(String playId) {
		this.playId = playId;
	}
	public PlayerInfo getPlayerInfo() {
		return playerInfo;
	}
	public void setPlayerInfo(PlayerInfo playerInfo) {
		this.playerInfo = playerInfo;
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
		return "GameRequest [gameRequestType=" + gameRequestType + ", playId=" + playId + ", playerInfo=" + playerInfo
				+ ", gameState=" + gameState + ", playerList=" + playerList + "]";
	}
	
	
	
	
}
