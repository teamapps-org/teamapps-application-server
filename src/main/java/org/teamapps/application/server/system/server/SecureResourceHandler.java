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
package org.teamapps.application.server.system.server;


import org.teamapps.ux.resource.Resource;
import org.teamapps.ux.resource.ResourceProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecureResourceHandler implements ResourceProvider {

	public static final String HANDLER_PREFIX = "/TA-SEC-PRVD/";
	private Map<String, SecureLinkBuilder> resourceProviderByHandlerKey = new ConcurrentHashMap<>();

	private static final SecureResourceHandler INSTANCE = new SecureResourceHandler();

	public static SecureResourceHandler getInstance() {
		return INSTANCE;
	}

	private SecureResourceHandler() {
	}

	public SecureLinkBuilder registerByteArrayResourceHandler(ByteArrayResourceProvider resourceProvider, String fileSuffix) {
		String handlerKey = getUUID(12);
		SecureLinkBuilder secureLinkBuilder = new SecureLinkBuilder(HANDLER_PREFIX + handlerKey, fileSuffix, resourceProvider);
		resourceProviderByHandlerKey.put(handlerKey, secureLinkBuilder);
		return secureLinkBuilder;
	}

	@Override
	public Resource getResource(String servletPath, String relativeResourcePath, String httpSessionId) {
		String[] parts = relativeResourcePath.split("/");
		if (parts.length > 2) {
			SecureLinkBuilder secureLinkBuilder = resourceProviderByHandlerKey.get(parts[1]);
			if (secureLinkBuilder != null) {
				return secureLinkBuilder.getResource(parts[2]);
			}
		}
		return null;
	}

	private String getUUID(int length) {
		String id = UUID.randomUUID().toString().replace("-", "");
		return id.substring(0, Math.min(id.length(), length));
	}

}
