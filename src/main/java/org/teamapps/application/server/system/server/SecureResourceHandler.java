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
