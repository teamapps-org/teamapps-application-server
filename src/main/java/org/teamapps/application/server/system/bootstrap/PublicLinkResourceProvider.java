package org.teamapps.application.server.system.bootstrap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.ux.resource.Resource;
import org.teamapps.ux.resource.ResourceProvider;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PublicLinkResourceProvider implements ResourceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String SERVLET_PATH_PREFIX = "/pl/";
	private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

	private final String serverUrl;
	private final Map<String, Resource> resourceByUuid = new ConcurrentHashMap<>();

	public PublicLinkResourceProvider(String serverUrl) {
		this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
	}

	@Override
	public Resource getResource(String servletPath, String relativeResourcePath, String httpSessionId) {
		return resourceByUuid.get(relativeResourcePath.replace("/", ""));
	}

	public String createLinkForResource(Resource resource, Duration availabilityDuration) {
		String suffix = FilenameUtils.getExtension(resource.getName());
		String linkName = UUID.randomUUID().toString() + (StringUtils.isNotBlank(suffix) ? "." + suffix : "");
		resourceByUuid.put(linkName, resource);
		SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
			resourceByUuid.remove(linkName);
		}, availabilityDuration.toSeconds(), TimeUnit.SECONDS);
		String link = serverUrl + SERVLET_PATH_PREFIX + linkName;
		LOGGER.info("Generating link for resource {} --> {}", resource.getName(), link);
		return link;
	}
}
