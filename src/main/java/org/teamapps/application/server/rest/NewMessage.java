package org.teamapps.application.server.rest;

public class NewMessage {

	private String channelId;
	private String message;
	private long timestamp;

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "NewMessage{" +
				"channelId='" + channelId + '\'' +
				", message='" + message + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}
