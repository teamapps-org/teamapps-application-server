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
package org.teamapps.application.server;

import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.index.log.ChunkedIndexMessageStore;
import org.teamapps.universaldb.index.log.MessageStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServerRegistry {
	private final UniversalDB universalDB;
	private final ChunkedIndexMessageStore<SystemLogEntry> systemLogMessageStore;
	private final Supplier<List<SessionHandler>> sessionHandlerSupplier;
	private EntityUpdateEventHandler entityUpdateEventHandler;
	private Map<String, Object> loadedApplications = new ConcurrentHashMap<>();

	public ServerRegistry(UniversalDB universalDB, ChunkedIndexMessageStore<SystemLogEntry> systemLogMessageStore, Supplier<List<SessionHandler>> sessionHandlerSupplier) {
		this.universalDB = universalDB;
		entityUpdateEventHandler = new EntityUpdateEventHandler(universalDB.getUpdateEventQueue());
		this.systemLogMessageStore = systemLogMessageStore;
		this.sessionHandlerSupplier = sessionHandlerSupplier;
	}

	public UniversalDB getUniversalDB() {
		return universalDB;
	}

	public EntityUpdateEventHandler getEntityUpdateEventHandler() {
		return entityUpdateEventHandler;
	}

	public Map<String, Object> getLoadedApplications() {
		return loadedApplications;
	}

	public List<SessionHandler> getSessionHandlers() {
		return sessionHandlerSupplier.get();
	}

	public ChunkedIndexMessageStore<SystemLogEntry> getSystemLogMessageStore() {
		return systemLogMessageStore;
	}
}
