/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2024 TeamApps.org
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

import org.teamapps.ux.resource.ByteArrayResource;
import org.teamapps.ux.resource.Resource;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecureLinkBuilder {

	private final String linkPrefix;
	private final String fileSuffix;
	private final ByteArrayResourceProvider byteArrayResourceProvider;
	private final LastModifiedProvider lastModifiedProvider;
	private final Map<String, Long> idByKey = new ConcurrentHashMap<>();
	private final Map<Long, String> keyById = new ConcurrentHashMap<>();

	public SecureLinkBuilder(String linkPrefix, String fileSuffix, ByteArrayResourceProvider byteArrayResourceProvider, LastModifiedProvider lastModifiedProvider) {
		this.linkPrefix = linkPrefix;
		this.fileSuffix = fileSuffix;
		this.byteArrayResourceProvider = byteArrayResourceProvider;
		this.lastModifiedProvider = lastModifiedProvider;
	}

	public String createLink(int entityId) {
		long id = entityId + ((lastModifiedProvider.getLastModified(entityId) / 1000) * 100_000_000);

		String key = keyById.get(id);
		if (key != null) {
			return linkPrefix + "/" + key;
		} else {
			if (byteArrayResourceProvider.getResource(entityId) == null) {
				return null;
			} else {
				String uuid = UUID.randomUUID().toString();
				idByKey.put(uuid, id);
				keyById.put(id, uuid);
				return linkPrefix + "/" + uuid;
			}
		}
	}

	public Resource getResource(String key) {
		if (idByKey.containsKey(key)) {
			long value = idByKey.get(key);
			int id = (int) (value % 100_000_000);
			return new ByteArrayResource(byteArrayResourceProvider.getResource(id), key + "." + fileSuffix)
					.lastModified(new Date(lastModifiedProvider.getLastModified(id)))
					.expiring(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 90));
		} else {
			return null;
		}
	}

}
