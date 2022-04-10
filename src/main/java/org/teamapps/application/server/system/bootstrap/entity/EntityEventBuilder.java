package org.teamapps.application.server.system.bootstrap.entity;

import org.teamapps.application.api.application.entity.EntityUpdate;
import org.teamapps.application.api.application.entity.EntityUpdateType;
import org.teamapps.event.Event;
import org.teamapps.universaldb.record.EntityBuilder;
import org.teamapps.universaldb.update.RecordUpdateEvent;

public class EntityEventBuilder<ENTITY> {

	private EntityBuilder<ENTITY> entityBuilder;
	private final Event<EntityUpdate<ENTITY>> event = new Event<>();

	public EntityEventBuilder(EntityBuilder<ENTITY> entityBuilder) {
		this.entityBuilder = entityBuilder;
	}

	public void fireEvent(RecordUpdateEvent recordUpdateEvent) {
		ENTITY entity = entityBuilder.build(recordUpdateEvent.getRecordId());
		event.fire(new EntityUpdate<>(entity, recordUpdateEvent.getUserId(), EntityUpdateType.create(recordUpdateEvent.getType())));
	}

	public Event<EntityUpdate<ENTITY>> getEvent() {
		return event;
	}

	public void updateBuilder(EntityBuilder<ENTITY> entityBuilder) {
		this.entityBuilder = entityBuilder;
	}
}
