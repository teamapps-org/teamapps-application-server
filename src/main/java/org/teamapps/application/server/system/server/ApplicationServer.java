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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.application.server.system.bootstrap.PublicLinkResourceProvider;
import org.teamapps.config.TeamAppsConfiguration;
import org.teamapps.model.ApplicationServerSchema;
import org.teamapps.model.system.SystemStarts;
import org.teamapps.model.system.Type;
import org.teamapps.server.ServletRegistration;
import org.teamapps.server.undertow.embedded.TeamAppsUndertowEmbeddedServer;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.ux.resource.ClassPathResourceProvider;
import org.teamapps.ux.resource.FileResource;
import org.teamapps.ux.resource.ResourceProviderServlet;
import org.teamapps.ux.session.SessionContext;
import org.teamapps.webcontroller.WebController;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.*;

public class ApplicationServer implements WebController, SessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final File basePath;
	private final File configPath;
	private TeamAppsConfiguration teamAppsConfiguration;
	private int port;
	private UniversalDB universalDb;

	private List<ServletRegistration> servletRegistrations = new ArrayList<>();
	private SessionHandler sessionHandler;

	private WeakHashMap<SessionHandler, Long> weakStartDateBySessionHandler = new WeakHashMap<>();

	public ApplicationServer(File basePath) {
		this(basePath, new File(basePath, "config"), new TeamAppsConfiguration(), 8080);
	}

	public ApplicationServer(File basePath, File configPath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		this.basePath = basePath;
		this.configPath = configPath;
		this.teamAppsConfiguration = teamAppsConfiguration;
		this.port = port;
		configPath.mkdir();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			SystemStarts.create().setTimestamp(Instant.now()).setType(Type.STOP).save();
		}));
	}

	public void updateSessionHandler(File jarFile) throws Exception {
		LOGGER.info("Loading new session handler:" + jarFile.getPath());
		URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
		SessionHandler newSessionHandler = loadSessionHandler(classLoader);
		newSessionHandler.init(this, universalDb, configPath);
		weakStartDateBySessionHandler.put(newSessionHandler, System.currentTimeMillis());
		this.sessionHandler = newSessionHandler;
		System.gc();
		LOGGER.info("Updated session handler:" + sessionHandler);
	}

	@Override
	public void updateSessionHandler(SessionHandler sessionHandler) {
		sessionHandler.init(this, universalDb, configPath);
		weakStartDateBySessionHandler.put(sessionHandler, System.currentTimeMillis());
		this.sessionHandler = sessionHandler;
		LOGGER.info("Updated fixed session handler:" + sessionHandler);
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
		Class<?> builder = classInfos.get(0).loadClass();
		return (SessionHandler) builder.getDeclaredConstructor().newInstance();
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
		File dbPath = new File(basePath, "db");
		dbPath.mkdir();
		universalDb = UniversalDB.createStandalone(dbPath, new ApplicationServerSchema());
		sessionHandler.init(this, universalDb, configPath);
		TeamAppsUndertowEmbeddedServer server = new TeamAppsUndertowEmbeddedServer(this, teamAppsConfiguration, port);

		addClassPathResourceProvider("org.teamapps.application.server.media", "/ta-media/");

		File staticResourcesPath = new File(basePath, "static");
		staticResourcesPath.mkdir();
		addServletRegistration(new ServletRegistration(new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> {
			try {
				File file = new File(staticResourcesPath, relativeResourcePath);
				if (file.exists() && !file.isDirectory() && file.toPath().toRealPath().startsWith(staticResourcesPath.getPath())) {
					return new FileResource(file);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}), "/static/*"));

		for (ServletRegistration servletRegistration : servletRegistrations) {
			for (String mapping : servletRegistration.getMappings()) {
				LOGGER.info("Registering servlet on url path: " + mapping);
				server.addServletContextListener(new ServletContextListener() {
					@Override
					public void contextInitialized(ServletContextEvent sce) {
						javax.servlet.ServletRegistration.Dynamic dynamic = sce.getServletContext().addServlet("teamapps-registered-" + servletRegistration.getServlet().getClass().getSimpleName() + UUID.randomUUID().toString(), servletRegistration.getServlet());
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
				servletContext.addServlet("ta-sec-links", new ResourceProviderServlet((servletPath, relativeResourcePath, httpSessionId) -> SecureResourceHandler.getInstance().getResource(servletPath, relativeResourcePath, httpSessionId)))
						.addMapping(SecureResourceHandler.HANDLER_PREFIX + "*");
				servletContext.addServlet("public-link-servlet", new ResourceProviderServlet(PublicLinkResourceProvider.getInstance())).addMapping(PublicLinkResourceProvider.SERVLET_PATH_PREFIX + "*");
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

}
