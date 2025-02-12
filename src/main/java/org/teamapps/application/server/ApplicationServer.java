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

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.config.TeamAppsConfiguration;
import org.teamapps.core.TeamAppsCore;
import org.teamapps.protocol.system.LoginData;
import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.server.undertow.embedded.TeamAppsUndertowEmbeddedServer;
import org.teamapps.universaldb.DatabaseManager;
import org.teamapps.universaldb.message.MessageStore;
import org.teamapps.ux.resource.FileResource;
import org.teamapps.ux.servlet.resourceprovider.ClassPathResourceProvider;
import org.teamapps.ux.servlet.resourceprovider.ResourceProviderServlet;
import org.teamapps.ux.session.SessionContext;
import org.teamapps.webcontroller.WebController;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationServer implements WebController, SessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationServerConfig serverConfig;
	private ServerRegistry serverRegistry;

	private List<ServletRegistration> servletRegistrations = new ArrayList<>();
	private SessionHandler sessionHandler;
	private DatabaseManager databaseManager;

	private WeakHashMap<SessionHandler, Long> weakStartDateBySessionHandler = new WeakHashMap<>();
	private TeamAppsCore teamAppsCore;

	public ApplicationServer(ApplicationServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}

	public ApplicationServer() {
		this.serverConfig = ApplicationServerConfig.create();
	}

	public ApplicationServer(ServerMode serverMode, File basePath) {
		this.serverConfig = ApplicationServerConfig.create(serverMode, basePath);
	}

	public ApplicationServer(ServerMode serverMode, File basePath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		this.serverConfig = ApplicationServerConfig.create(serverMode, basePath, teamAppsConfiguration, port);
	}

	public SessionHandler updateSessionHandler(File jarFile) throws Exception {
		LOGGER.info("Loading new session handler:" + jarFile.getPath());
		URL resource = jarFile.toURI().toURL();
		return updateSessionHandler(resource);
	}

	public SessionHandler updateSessionHandler(URL resource) throws Exception {
		LOGGER.info("Loading new session handler:" + resource.getPath());
		URLClassLoader classLoader = new URLClassLoader(new URL[]{resource});
		SessionHandler newSessionHandler = loadSessionHandler(classLoader);
		if (newSessionHandler != null) {
			weakStartDateBySessionHandler.put(newSessionHandler, System.currentTimeMillis());
			if (serverRegistry != null) {
				newSessionHandler.init(this, serverRegistry);
			}
			this.sessionHandler = newSessionHandler;
			System.gc();
			LOGGER.info("Updated session handler:" + sessionHandler);
		}
		return newSessionHandler;
	}

	@Override
	public void updateSessionHandler(SessionHandler sessionHandler) {
		weakStartDateBySessionHandler.put(sessionHandler, System.currentTimeMillis());
		if (serverRegistry != null) {
			sessionHandler.init(this, serverRegistry);
		}
		this.sessionHandler = sessionHandler;
		System.gc();
		LOGGER.info("Updated session handler:" + sessionHandler);
	}

	private SessionHandler loadSessionHandler(URLClassLoader classLoader) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		ClassGraph classGraph = new ClassGraph();
		if (classLoader != null) {
			classGraph.overrideClassLoaders(classLoader);
		}

		ClassInfoList classInfos = classGraph
				.enableAllInfo()
				.scan()
				.getClassesImplementing(SessionHandler.class.getName())
				.getStandardClasses();

		ClassInfo bootableClassInfo = null;
		for (ClassInfo classInfo : classInfos) {
			if (bootableClassInfo == null) {
				bootableClassInfo = classInfo;
			} else {
				String path1 = getInheritanceClassPath(classInfo);
				String path2 = getInheritanceClassPath(bootableClassInfo);
				if (path1.contains(path2)) {
					bootableClassInfo = classInfo;
				} else if (!path2.contains(path1)) {
					bootableClassInfo = null;
					LOGGER.error("Error several implementations for SessionHandler without common inheritance");
					break;
				}
			}
		}

		if (bootableClassInfo == null) {
			LOGGER.info("Selecting class by annotation...");
			bootableClassInfo = classInfos.stream().sorted((o1, o2) -> Integer.compare(getBootPriority(o2), getBootPriority(o1))).findFirst().orElse(null);
		}

		if (bootableClassInfo != null) {
			LOGGER.info("Booting class: " + bootableClassInfo.getName() + " with priority:" + getBootPriority(bootableClassInfo));
			Class<?> builder = classLoader.loadClass(bootableClassInfo.getName());
			return (SessionHandler) builder.getDeclaredConstructor().newInstance();
		} else {
			LOGGER.error("Error: no matching class found!");
			return null;
		}
	}

	private int getBootPriority(ClassInfo classInfo) {
		AnnotationInfo bootableClass = classInfo.getAnnotationInfo("org.teamapps.application.api.annotation.TeamAppsBootableClass");
		return bootableClass != null ? (int) bootableClass.getParameterValues(true).getValue("priority") : 0;
	}

	private String getInheritanceClassPath(ClassInfo classInfo) {
		List<String> path = new ArrayList<>();
		path.add(classInfo.getName());
		for (ClassInfo superclass : classInfo.getSuperclasses()) {
			path.add(superclass.getName());
		}
		Collections.reverse(path);
		return path.stream().collect(Collectors.joining("/"));
	}


	public void setSessionHandler(SessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;
	}

	@Override
	public void onSessionStart(SessionContext context) {
		sessionHandler.handleSessionStart(context);
	}

	@Override
	public Collection<Long> getBootstrappedSystems() {
		return weakStartDateBySessionHandler.values();
	}

	public void start() throws Exception {
		LOGGER.info("START TEAMAPPS.ORG APPLICATION SERVER with: \nport: {}, \nserver-mode: {}, \npaths: \n\tindex-path: {}, \n\tfull-text-path: {}, \n\ttransaction-log-path: {}, \n\tfile-store-path: {}, \n\tapp-data-path: {}, \n\tembedded-content-path: {}, \n\twebserver-path: {}, \n\ttemp-path: {}",
				serverConfig.getPort(), serverConfig.getServerMode(),
				serverConfig.getIndexPath().toPath(), serverConfig.getFullTextIndexPath().toPath(), serverConfig.getTransactionLogPath().toPath(),
				serverConfig.getFileStorePath().toPath(), serverConfig.getAppDataPath().toPath(), serverConfig.getEmbeddedContentStorePath().toPath(),
				serverConfig.getWebserverStaticFilesPath().toPath(), serverConfig.getTempPath().toPath());

		databaseManager = new DatabaseManager();
		MessageStore<SystemLogEntry> logMessageStore = MessageStore.create(serverConfig.getLogStorePath(), "system-logs", SystemLogEntry.getMessageDecoder());
		DatabaseLogAppender.startLogger(logMessageStore);
		MessageStore<LoginData> loginDataMessageStore = MessageStore.create(serverConfig.getLogStorePath(), "login-logs", LoginData.getMessageDecoder());

		TeamAppsUndertowEmbeddedServer server = new TeamAppsUndertowEmbeddedServer(this, serverConfig.getTeamAppsConfiguration(), serverConfig.getPort());
		teamAppsCore = server.getTeamAppsCore();
		serverRegistry = new ServerRegistry(serverConfig, databaseManager, logMessageStore, loginDataMessageStore, () -> weakStartDateBySessionHandler.keySet().stream().filter(Objects::nonNull).collect(Collectors.toList()), teamAppsCore);
		sessionHandler.init(this, serverRegistry);

		addClassPathResourceProvider("org.teamapps.application.server.media", "/ta-media/");

		addServletRegistration(new ServletRegistration(new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> {
			File file = new File(serverConfig.getWebserverStaticFilesPath(), relativeResourcePath);
			if (file.exists() && !file.isDirectory()) {
				return new FileResource(file);
			}
			return null;
		}), "/static/*"));

		for (ServletRegistration servletRegistration : servletRegistrations) {
			for (String mapping : servletRegistration.getMappings()) {
				LOGGER.info("Registering servlet on url path: " + mapping);
				server.addServletContextListener(new ServletContextListener() {
					@Override
					public void contextInitialized(ServletContextEvent sce) {
						jakarta.servlet.ServletRegistration.Dynamic dynamic = sce.getServletContext().addServlet("teamapps-registered-" + servletRegistration.getServlet().getClass().getSimpleName() + UUID.randomUUID().toString(), servletRegistration.getServlet());
						dynamic.setAsyncSupported(servletRegistration.isAsyncSupported());
						dynamic.addMapping(mapping);
					}
				});
			}
		}
		server.addServletContextListener(new ServletContextListener() {
			@Override
			public void contextInitialized(ServletContextEvent sce) {
				ServletContext servletContext = sce.getServletContext();
				servletContext.addServlet("ta-embedded", new ResourceProviderServlet(new EmbeddedResourceStore(serverConfig.getEmbeddedContentStorePath()))).addMapping(EmbeddedResourceStore.RESOURCE_PREFIX + "*");
				servletContext.addServlet("ta-sec-links", new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> SecureResourceHandler.getInstance().getResource(servletPath, relativeResourcePath, httpSessionId))).addMapping(SecureResourceHandler.HANDLER_PREFIX + "*");
				servletContext.addServlet("ta-public-link", new ResourceProviderServlet(PublicLinkResourceProvider.getInstance())).addMapping(PublicLinkResourceProvider.SERVLET_PATH_PREFIX + "*");
				servletContext.addServlet("ta-api-servlet", ApiServlet.getInstance()).addMapping("/api/*");
			}
		});

		server.start();
	}

	public void addClassPathResourceProvider(String basePackage, String prefix) {
		if (!prefix.endsWith("/")) {
			prefix += "/";
		}
		addServletRegistration(new ServletRegistration(new ResourceProviderServlet(new ClassPathResourceProvider(basePackage)), prefix + "*"));
	}

	public void addServletRegistration(ServletRegistration servletRegistration) {
		this.servletRegistrations.add(servletRegistration);
	}

	public TeamAppsCore getTeamAppsCore() {
		return teamAppsCore;
	}

	public ApplicationServerConfig getServerConfig() {
		return serverConfig;
	}
}
