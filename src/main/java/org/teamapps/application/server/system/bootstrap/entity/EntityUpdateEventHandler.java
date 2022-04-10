package org.teamapps.application.server.system.bootstrap.entity;

import org.teamapps.application.api.application.entity.EntityUpdate;
import org.teamapps.universaldb.record.EntityBuilder;
import org.teamapps.universaldb.update.RecordUpdateEvent;
import org.teamapps.ux.session.SessionContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EntityUpdateEventHandler extends Thread {

	private final ArrayBlockingQueue<RecordUpdateEvent> updateQueue;
	private final ConcurrentHashMap<Integer, EntityEventBuilder> entityEventBuilderMap = new ConcurrentHashMap<>();

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
				EntityEventBuilder entityEventBuilder = entityEventBuilderMap.get(updateEvent.getTableId());
				if (entityEventBuilder != null) {
					entityEventBuilder.fireEvent(updateEvent);
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

		Consumer<EntityUpdate<ENTITY>> consumer = entityEntityUpdate -> {
			if (userId != entityEntityUpdate.getUserId()) {
				listener.accept(entityEntityUpdate);
			}
		};

		EntityEventBuilder<ENTITY> entityEventBuilder = entityEventBuilderMap.get(entityBuilder.getTableId());
		if (entityEventBuilder == null) {
			entityEventBuilder = new EntityEventBuilder<>(entityBuilder);
			entityEventBuilderMap.put(entityBuilder.getTableId(), entityEventBuilder);
		} else {
			//the builder might get replaced with an app update - so always keep a reference to a current builder
			entityEventBuilder.updateBuilder(entityBuilder);
		}
		entityEventBuilder.getEvent().addListener(consumer);
	}
}
