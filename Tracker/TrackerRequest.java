package Tracker;

import java.io.Serializable;

public class TrackerRequest implements Serializable {
	private static final long serialVersionUID = 623458965242268876L;
	public enum TrackerRequestType {
		JOIN(0), PLAYER_LEAVE(1);
		private int type;
		private TrackerRequestType(int type) {
			this.type = type;
		}
		private int getType() {
			return type;
		}
	}
	
	private TrackerRequestType trackerRequestType;
	private String playId;
	
	public TrackerRequest(TrackerRequestType trackerRequestType, String playId) {
		super();
		this.trackerRequestType = trackerRequestType;
		this.playId = playId;
	}
	public TrackerRequestType getTrackerRequestType() {
		return trackerRequestType;
	}
	public void setTrackerRequestType(TrackerRequestType trackerRequestType) {
		this.trackerRequestType = trackerRequestType;
	}
	public String getPlayId() {
		return playId;
	}
	public void setPlayId(String playId) {
		this.playId = playId;
	}
	
	@Override
	public String toString() {
		return "TrackerRequest [trackerRequestType=" + trackerRequestType + ", playId=" + playId + "]";
	}
}
