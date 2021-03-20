package org.teamapps.application.server.system.server;

import org.teamapps.ux.resource.ByteArrayResource;
import org.teamapps.ux.resource.Resource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecureLinkBuilder {

	private final String linkPrefix;
	private final String fileSuffix;
	private final ByteArrayResourceProvider byteArrayResourceProvider;
	private final Map<String, Integer> idByKey = new ConcurrentHashMap<>();
	private final Map<Integer, String> keyById = new ConcurrentHashMap<>();

	public SecureLinkBuilder(String linkPrefix, String fileSuffix, ByteArrayResourceProvider byteArrayResourceProvider) {
		this.linkPrefix = linkPrefix;
		this.fileSuffix = fileSuffix;
		this.byteArrayResourceProvider = byteArrayResourceProvider;
	}

	public String createLink(int id) {
		String key = keyById.get(id);
		if (key != null) {
			return linkPrefix + "/" + key;
		} else {
			if (byteArrayResourceProvider.getResource(id) == null) {
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
		Integer id = idByKey.get(key);
		return id != null ? new ByteArrayResource(byteArrayResourceProvider.getResource(id), key + "." + fileSuffix) : null;
	}

}
