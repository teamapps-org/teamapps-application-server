/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2025 TeamApps.org
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

import org.teamapps.core.TeamAppsCore;
import org.teamapps.protocol.system.LoginData;
import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.universaldb.DatabaseManager;
import org.teamapps.universaldb.message.MessageStore;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServerRegistry {
	private final ApplicationServerConfig serverConfig;
	private final DatabaseManager databaseManager;
	private final MessageStore<SystemLogEntry> systemLogMessageStore;
	private final MessageStore<LoginData> loginDataMessageStore;
	private final Supplier<List<SessionHandler>> sessionHandlerSupplier;
	private final TeamAppsCore teamAppsCore;
	private final Map<String, EntityUpdateEventHandler> databaseEventHandlerMap;
	private Map<String, Object> loadedApplications = new ConcurrentHashMap<>();

	public ServerRegistry(ApplicationServerConfig serverConfig, DatabaseManager databaseManager, MessageStore<SystemLogEntry> systemLogMessageStore, MessageStore<LoginData> loginDataMessageStore, Supplier<List<SessionHandler>> sessionHandlerSupplier, TeamAppsCore teamAppsCore) {
		this.serverConfig = serverConfig;
		this.databaseManager = databaseManager;
		this.databaseEventHandlerMap = new ConcurrentHashMap<>();
		this.systemLogMessageStore = systemLogMessageStore;
		this.loginDataMessageStore = loginDataMessageStore;
		this.sessionHandlerSupplier = sessionHandlerSupplier;
		this.teamAppsCore = teamAppsCore;
		databaseManager.addDatabaseHandler(universalDB -> {
			databaseEventHandlerMap.put(universalDB.getName(), new EntityUpdateEventHandler(universalDB.getUpdateEventQueue()));
		});
	}

	public ApplicationServerConfig getServerConfig() {
		return serverConfig;
	}

	public File getAppsBasePath() {
		return serverConfig.getAppDataPath();
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public EntityUpdateEventHandler getEntityUpdateEventHandler(String name) {
		return databaseEventHandlerMap.get(name);
	}

	public Map<String, Object> getLoadedApplications() {
		return loadedApplications;
	}

	public List<SessionHandler> getSessionHandlers() {
		return sessionHandlerSupplier.get();
	}

	public MessageStore<SystemLogEntry> getSystemLogMessageStore() {
		return systemLogMessageStore;
	}

	public MessageStore<LoginData> getLoginDataMessageStore() {
		return loginDataMessageStore;
	}

	public TeamAppsCore getTeamAppsCore() {
		return teamAppsCore;
	}
}
