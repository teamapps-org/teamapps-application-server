package org.teamapps.application.server.rest;

public class Message {

	private final String messageId;
	private final Author author;
	private final String message;

	public Message(String messageId, Author author, String message) {
		this.messageId = messageId;
		this.author = author;
		this.message = message;
	}

	public String getMessageId() {
		return messageId;
	}

	public Author getAuthor() {
		return author;
	}

	public String getMessage() {
		return message;
	}
}
