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
package org.teamapps.application.server;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.docx4j.wml.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.cluster.network.NodeAddress;
import org.teamapps.config.TeamAppsConfiguration;
import org.teamapps.model.ApplicationServerSchema;
import org.teamapps.model.system.SystemStarts;
import org.teamapps.model.system.Type;
import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.server.undertow.embedded.TeamAppsUndertowEmbeddedServer;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.index.log.MessageStore;
import org.teamapps.ux.resource.FileResource;
import org.teamapps.ux.servlet.resourceprovider.ClassPathResourceProvider;
import org.teamapps.ux.servlet.resourceprovider.ResourceProviderServlet;
import org.teamapps.ux.session.SessionContext;
import org.teamapps.webcontroller.WebController;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationServer implements WebController, SessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private File basePath;
	private TeamAppsConfiguration teamAppsConfiguration;
	private int port;
	private UniversalDB universalDb;
	private ServerRegistry serverRegistry;

	private List<ServletRegistration> servletRegistrations = new ArrayList<>();
	private SessionHandler sessionHandler;

	private WeakHashMap<SessionHandler, Long> weakStartDateBySessionHandler = new WeakHashMap<>();

	private boolean useCluster;
	private String clusterSecret;
	private int leaderPort;
	private String leaderHost;
	private int localPort;

	public ApplicationServer() {
		this(new File("./server-data"), new TeamAppsConfiguration(), 8080);
	}

	public ApplicationServer(boolean useCluster, String clusterSecret, int localPort, String leaderHost, int leaderPort) {
		this.basePath = new File("./server-data");
		this.teamAppsConfiguration = new TeamAppsConfiguration();
		this.port = 8080;

		this.useCluster = useCluster;
		this.clusterSecret = clusterSecret;
		this.leaderPort = leaderPort;
		this.leaderHost = leaderHost;
		this.localPort = localPort;
	}

	public ApplicationServer(File basePath) {
		this(basePath, new TeamAppsConfiguration(), 8080);
	}

	public ApplicationServer(File basePath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		this.basePath = basePath;
		this.teamAppsConfiguration = teamAppsConfiguration;
		this.port = port;
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

	private SessionHandler loadSessionHandler(URLClassLoader classLoader) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
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
			Class<?> builder = bootableClassInfo.loadClass();
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
		File dbPath = new File(basePath, "database");
		dbPath.mkdir();
		File embeddedStore = new File(basePath, "embeddedStore");
		embeddedStore.mkdir();
		if (useCluster) {
			if (leaderHost != null) {
				universalDb = new UniversalDB(dbPath, new ApplicationServerSchema(), clusterSecret, localPort, new NodeAddress(leaderHost, leaderPort));
			} else {
				universalDb = new UniversalDB(dbPath, new ApplicationServerSchema(), clusterSecret, localPort);
			}
		} else {
			universalDb = UniversalDB.createStandalone(dbPath, new ApplicationServerSchema());
		}
		MessageStore<SystemLogEntry> logMessageStore = new MessageStore<>(basePath, "systemLog", SystemLogEntry.getMessageDecoder());
		DatabaseLogAppender.startLogger();
		serverRegistry = new ServerRegistry(universalDb, logMessageStore, () -> weakStartDateBySessionHandler.keySet().stream().filter(Objects::nonNull).collect(Collectors.toList()));
		sessionHandler.init(this, serverRegistry);
		TeamAppsUndertowEmbeddedServer server = new TeamAppsUndertowEmbeddedServer(this, teamAppsConfiguration, port);

		addClassPathResourceProvider("org.teamapps.application.server.media", "/ta-media/");

		File staticResourcesPath = new File(basePath, "static");
		staticResourcesPath.mkdir();

		addServletRegistration(new ServletRegistration(new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> {
			File file = new File(staticResourcesPath, relativeResourcePath);
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
				servletContext.addServlet("ta-embedded", new ResourceProviderServlet(new EmbeddedResourceStore(embeddedStore))).addMapping(EmbeddedResourceStore.RESOURCE_PREFIX + "*");
				servletContext.addServlet("ta-sec-links", new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> SecureResourceHandler.getInstance().getResource(servletPath, relativeResourcePath, httpSessionId))).addMapping(SecureResourceHandler.HANDLER_PREFIX + "*");
				servletContext.addServlet("ta-public-link", new ResourceProviderServlet(PublicLinkResourceProvider.getInstance())).addMapping(PublicLinkResourceProvider.SERVLET_PATH_PREFIX + "*");
			}
		});

		server.start();
		SystemStarts.create().setTimestamp(Instant.now()).setType(Type.START).save();
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

	public void setTeamAppsConfiguration(TeamAppsConfiguration teamAppsConfiguration) {
		this.teamAppsConfiguration = teamAppsConfiguration;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBasePath(File basePath) {
		this.basePath = basePath;
	}

}
