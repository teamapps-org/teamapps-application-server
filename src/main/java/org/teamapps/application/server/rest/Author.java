package org.teamapps.application.server.rest;

public class Author {

	private final String id;
	private final String name;
	private final long lastAvatarUpdate;

	public Author(String id, String name, long lastAvatarUpdate) {
		this.id = id;
		this.name = name;
		this.lastAvatarUpdate = lastAvatarUpdate;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getLastAvatarUpdate() {
		return lastAvatarUpdate;
	}
}
