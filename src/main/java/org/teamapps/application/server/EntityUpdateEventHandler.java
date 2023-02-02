/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2023 TeamApps.org
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

import org.teamapps.application.api.application.entity.EntityUpdate;
import org.teamapps.application.api.application.entity.EntityUpdateType;
import org.teamapps.event.Event;
import org.teamapps.universaldb.record.EntityBuilder;
import org.teamapps.universaldb.update.RecordUpdateEvent;
import org.teamapps.ux.session.SessionContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EntityUpdateEventHandler extends Thread {

	private final ArrayBlockingQueue<RecordUpdateEvent> updateQueue;
	private final ConcurrentHashMap<Integer, TableEventHandler> tableEventHandlerByTableId = new ConcurrentHashMap<>();

	public EntityUpdateEventHandler(ArrayBlockingQueue<RecordUpdateEvent> updateQueue) {
		this.updateQueue = updateQueue;
		setDaemon(true);
		start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				RecordUpdateEvent updateEvent = updateQueue.take();
				TableEventHandler tableEventHandler = tableEventHandlerByTableId.get(updateEvent.getTableId());
				if (tableEventHandler != null) {
					tableEventHandler.fireEvent(updateEvent);
				}
			} catch (InterruptedException ignore) { }
		}
	}

	public synchronized <ENTITY> void registerEntity(EntityBuilder<ENTITY> entityBuilder, int userId, Consumer<EntityUpdate<ENTITY>> listener) {
		if (SessionContext.currentOrNull() == null) {
			throw new RuntimeException("Error: cannot register entity update listener without session context!");
		}
		if (userId <= 0) {
			throw new RuntimeException("Error: cannot register entity update listener without userId!");
		}

		Consumer<RecordUpdateEvent> recordUpdateEventConsumer = recordUpdateEvent -> {
			if (userId != recordUpdateEvent.getUserId()) {
				ENTITY entity = entityBuilder.build(recordUpdateEvent.getRecordId());
				EntityUpdate<ENTITY> entityEntityUpdate = new EntityUpdate<>(entity, recordUpdateEvent.getUserId(), EntityUpdateType.create(recordUpdateEvent.getType()));
				listener.accept(entityEntityUpdate);
			}
		};

		TableEventHandler tableEventHandler = tableEventHandlerByTableId.get(entityBuilder.getTableId());
		if (tableEventHandler ==  null) {
			tableEventHandler = new TableEventHandler(entityBuilder.getTableId());
			tableEventHandlerByTableId.put(entityBuilder.getTableId(), tableEventHandler);
		}
		tableEventHandler.addListener(recordUpdateEventConsumer);
	}

	public static class TableEventHandler {

		private final int tableId;
		private final Event<RecordUpdateEvent> event = new Event<>();

		public TableEventHandler(int tableId) {
			this.tableId = tableId;
		}

		public void fireEvent(RecordUpdateEvent recordUpdateEvent) {
			event.fire(recordUpdateEvent);
		}

		public void addListener(Consumer<RecordUpdateEvent> consumer) {
			event.addListener(consumer);
		}

		public int getTableId() {
			return tableId;
		}
	}

}

