package Tracker;

import java.io.Serializable;

public class PlayerInfo implements Serializable{

	private static final long serialVersionUID = -4490807448260691722L;

	private final String address;
	private final int port;
	
	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public PlayerInfo(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public String toString() {
		return "PlayerInfo [address=" + address + ", port=" + port + "]";
	}
}
