/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.rest;

public class Message {

	private final String messageId;
	private final Author author;
	private final String message;
	private final long timestamp;

	public Message(String messageId, Author author, String message, long timestamp) {
		this.messageId = messageId;
		this.author = author;
		this.message = message;
		this.timestamp = timestamp;
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
