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
