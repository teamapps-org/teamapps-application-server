/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2022 TeamApps.org
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
