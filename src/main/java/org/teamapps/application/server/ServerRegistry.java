package org.teamapps.application.server;

import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.index.log.MessageStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServerRegistry {
	private final UniversalDB universalDB;
	private final MessageStore<SystemLogEntry> systemLogMessageStore;
	private final Supplier<List<SessionHandler>> sessionHandlerSupplier;
	private EntityUpdateEventHandler entityUpdateEventHandler;
	private Map<String, Object> loadedApplications = new ConcurrentHashMap<>();

	public ServerRegistry(UniversalDB universalDB, MessageStore<SystemLogEntry> systemLogMessageStore, Supplier<List<SessionHandler>> sessionHandlerSupplier) {
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

	public MessageStore<SystemLogEntry> getSystemLogMessageStore() {
		return systemLogMessageStore;
	}
}
