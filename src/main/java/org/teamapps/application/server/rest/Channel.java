package org.teamapps.application.server.rest;

public class Channel {

	private final String channelId;
	private final String channelTitle;
	private final long lastAvatarUpdate;
	private final int newMessages;

	public Channel(String channelId, String channelTitle, long lastAvatarUpdate, int newMessages) {
		this.channelId = channelId;
		this.channelTitle = channelTitle;
		this.lastAvatarUpdate = lastAvatarUpdate;
		this.newMessages = newMessages;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getChannelTitle() {
		return channelTitle;
	}

	public long getLastAvatarUpdate() {
		return lastAvatarUpdate;
	}

	public int getNewMessages() {
		return newMessages;
	}
}
