package org.teamapps.application.server;

import org.teamapps.cluster.crypto.ShaHash;
import org.teamapps.universaldb.index.file.FileUtil;
import org.teamapps.ux.resource.FileResource;
import org.teamapps.ux.resource.Resource;
import org.teamapps.ux.servlet.resourceprovider.ResourceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedResourceStore implements ResourceProvider {
	public static final String RESOURCE_PREFIX = "/TA-EMBEDDED/";
	private static EmbeddedResourceStore INSTANCE;
	private final File basePath;
	private final Map<String, String> conversionMap = new ConcurrentHashMap<>();

	public static EmbeddedResourceStore getInstance() {
		return INSTANCE;
	}

	public EmbeddedResourceStore(File basePath) {
		this.basePath = basePath;
		mapDirectoryRecursive(basePath);
		INSTANCE = this;
	}

	public String saveResource(String application, String bucket, File file) throws IOException {
		if (file == null || file.length() == 0 || illegalName(application) || illegalName(bucket)) {
			return null;
		}
		String appKey = mapName(application);
		String bucketKey = mapName(bucket);
		String fileHash = FileUtil.createFileHash(file);
		File storeFile = new File(basePath, application + "/" + bucket + "/" + fileHash);
		if (!storeFile.exists()) {
			File path = storeFile.getParentFile();
			if (!path.exists()) {
				path.mkdirs();
			}
			Files.copy(file.toPath(), storeFile.toPath());
		}
		return RESOURCE_PREFIX + appKey + "/" + bucketKey + "/" + fileHash;
	}

	@Override
	public Resource getResource(String servletPath, String relativeResourcePath, String httpSessionId) {
		String[] parts = relativeResourcePath.split("/");
		if (parts.length > 3) {
			String application = conversionMap.get(parts[1]);
			String bucket = conversionMap.get(parts[2]);
			String fileHash = parts[3];
			File file = new File(basePath, application + "/" + bucket + "/" + fileHash);
			String name = fileHash;
			if (parts.length > 4) {
				name = parts[4];
			}
			if (file.exists()) {
				return new FileResource(file, name);
			}
		}
		return null;
	}

	private void mapDirectoryRecursive(File directory) {
		if (directory.isDirectory()) {
			String name = directory.getName();
			mapName(name);
			for (File file : directory.listFiles()) {
				mapDirectoryRecursive(file);
			}
		}
	}

	private String mapName(String name) {
		if (conversionMap.containsKey(name)) {
			return conversionMap.get(name);
		} else {
			String hash = ShaHash.createHash(name).substring(0, 16);
			conversionMap.put(hash, name);
			conversionMap.put(name, hash);
			return hash;
		}
	}

	private boolean illegalName(String name) {
		if (name == null || name.isBlank() || name.contains(" ") || name.contains("/") || name.contains("\\") || name.contains("'") || name.contains("\"")) {
			return true;
		} else {
			return false;
		}
	}



}
