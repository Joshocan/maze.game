package Tracker;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerList implements Serializable {

	private static final long serialVersionUID = 323792854918812634L;
	private final int n;
	private final int k;
	private Map<String, PlayerInfo> playInfos = new LinkedHashMap<>();
	
	public int getN() {
		return n;
	}
 
	public int getK() {
	
		return k;
	}

	public Entry<String, PlayerInfo> getPrimaryServerPlayerInfo() {
		return playInfos.entrySet().iterator().next();
	}

	public Entry<String, PlayerInfo> getBackupServerPlayerInfo() {
		Iterator<Entry<String, PlayerInfo>> iter = playInfos.entrySet().iterator();
		Entry<String, PlayerInfo> first = iter.next();
		return iter.hasNext() ? iter.next() : first;
	}

	public Map<String, PlayerInfo> getPlayInfos() {
		return playInfos;
	}
	

	public PlayerList(int n, int k) {
		this.n = n;
		this.k = k;
	}
	
	public void addPlayInfoById (String playId, PlayerInfo playerInfo) {
		playInfos.put(playId, playerInfo);
	}
	
	public void removePlayInfoById (String playId) {
		playInfos.remove(playId);
	}

	@Override
	public String toString() {
		return "PlayerList [n=" + n + ", k=" + k + ", playInfos=" + playInfos + "]";
	}
}
