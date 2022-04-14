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
