/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2023 TeamApps.org
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

import org.teamapps.config.TeamAppsConfiguration;

import java.io.File;

public class ApplicationServerConfig {

	private final File basePath;
	private final File dateBasePath;
	private final File fileStorePath;
	private final File messageStorePath;
	private final File logStorePath;
	private final File embeddedContentStorePath;
	private final File webserverStaticFilesPath;
	private final TeamAppsConfiguration teamAppsConfiguration;
	private final int port;

	public static ApplicationServerConfig create() {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		int port = 8080;
		File basePath = new File("./server-data");
		basePath.mkdir();
		return createPaths(teamAppsConfiguration, port, basePath);
	}

	public static ApplicationServerConfig create(File basePath) {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		int port = 8080;
		return createPaths(teamAppsConfiguration, port, basePath);
	}


	public static ApplicationServerConfig create(File basePath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		return createPaths(teamAppsConfiguration, port, basePath);
	}


	public static ApplicationServerConfig create(File basePath, File dateBasePath, File fileStorePath, File messageStorePath, File logStorePath, File embeddedContentStorePath, File webserverStaticFilesPath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		return new ApplicationServerConfig(basePath, dateBasePath, fileStorePath, messageStorePath, logStorePath, embeddedContentStorePath, webserverStaticFilesPath, teamAppsConfiguration, port);
	}

	private static ApplicationServerConfig createPaths(TeamAppsConfiguration teamAppsConfiguration, int port, File basePath) {
		File dateBasePath = createPath(basePath, "database");
		File fileStorePath = createPath(basePath, "file-store");
		File messageStorePath = createPath(basePath, "message-store");
		File logStorePath = createPath(basePath, "log-store");
		File embeddedContentStorePath = createPath(basePath, "embedded-content-store");
		File webserverStaticFilesPath = createPath(basePath, "static-files");
		return new ApplicationServerConfig(basePath, dateBasePath, fileStorePath, messageStorePath, logStorePath, embeddedContentStorePath, webserverStaticFilesPath, teamAppsConfiguration, port);
	}

	private ApplicationServerConfig(File basePath, File dateBasePath, File fileStorePath, File messageStorePath, File logStorePath, File embeddedContentStorePath, File webserverStaticFilesPath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		this.basePath = basePath;
		this.dateBasePath = dateBasePath;
		this.fileStorePath = fileStorePath;
		this.messageStorePath = messageStorePath;
		this.logStorePath = logStorePath;
		this.embeddedContentStorePath = embeddedContentStorePath;
		this.webserverStaticFilesPath = webserverStaticFilesPath;
		this.teamAppsConfiguration = teamAppsConfiguration;
		this.port = port;
	}

	private static File createPath(File basePath, String name) {
		File path = new File(basePath, name);
		path.mkdir();
		return path;
	}

	public File getBasePath() {
		return basePath;
	}

	public File getDateBasePath() {
		return dateBasePath;
	}

	public File getFileStorePath() {
		return fileStorePath;
	}

	public File getMessageStorePath() {
		return messageStorePath;
	}

	public File getLogStorePath() {
		return logStorePath;
	}

	public File getEmbeddedContentStorePath() {
		return embeddedContentStorePath;
	}

	public File getWebserverStaticFilesPath() {
		return webserverStaticFilesPath;
	}

	public TeamAppsConfiguration getTeamAppsConfiguration() {
		return teamAppsConfiguration;
	}

	public int getPort() {
		return port;
	}
}
