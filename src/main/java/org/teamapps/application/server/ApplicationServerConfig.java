/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2025 TeamApps.org
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.config.TeamAppsConfiguration;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class ApplicationServerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final File indexPath;
	private final File fullTextIndexPath;
	private final File transactionLogPath;
	private final File fileStorePath;
	private final File logStorePath;
	private final File appDataPath;
	private final File embeddedContentStorePath;
	private final File webserverStaticFilesPath;
	private final File tempPath;
	private final TeamAppsConfiguration teamAppsConfiguration;
	private final int port;

	public static ApplicationServerConfig create() {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		int port = 8080;
		ServerMode serverMode = ServerMode.DEVELOPMENT;
		File basePath = createPath(new File("./"), "server-data");
		return createPaths(teamAppsConfiguration, port, basePath);
	}

	public static ApplicationServerConfig create(File basePath) {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		teamAppsConfiguration.setCommandBufferTotalSize(30_000_000);
		teamAppsConfiguration.setCommandBufferLength(50_000);
		int port = 8080;
		return createPaths(teamAppsConfiguration, port, basePath);
	}

	public static ApplicationServerConfig create(File basePath, File fileStorePath) {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		teamAppsConfiguration.setCommandBufferTotalSize(30_000_000);
		teamAppsConfiguration.setCommandBufferLength(50_000);
		int port = 8080;
		return createPaths(teamAppsConfiguration, port, basePath, fileStorePath);
	}

	public static ApplicationServerConfig create(File basePath, File fileStorePath, int port) {
		TeamAppsConfiguration teamAppsConfiguration = new TeamAppsConfiguration();
		teamAppsConfiguration.setCommandBufferTotalSize(30_000_000);
		teamAppsConfiguration.setCommandBufferLength(50_000);
		return createPaths(teamAppsConfiguration, port, basePath, fileStorePath);
	}


	public static ApplicationServerConfig create(File basePath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		return createPaths(teamAppsConfiguration, port, basePath);
	}

	private static ApplicationServerConfig createPaths(TeamAppsConfiguration teamAppsConfiguration, int port, File basePath) {
		File indexPath = createPath(basePath, "index");
		File fullTextIndexPath = createPath(basePath, "text");
		File transactionLogPath = createPath(basePath, "transactions");
		File fileStorePath = createPath(basePath, "files");
		File logStorePath = createPath(basePath, "logs");
		File appDataPath = createPath(basePath, "apps");
		File embeddedContentStorePath = createPath(basePath, "embedded");
		File webserverStaticFilesPath = createPath(basePath, "static");
		File temp = createPath(basePath, "temp");
		return new ApplicationServerConfig(indexPath, fullTextIndexPath, transactionLogPath, fileStorePath, logStorePath, appDataPath, embeddedContentStorePath, webserverStaticFilesPath, temp, teamAppsConfiguration, port);
	}

	private static ApplicationServerConfig createPaths(TeamAppsConfiguration teamAppsConfiguration, int port, File basePath, File fileStorePath) {
		File indexPath = createPath(basePath, "index");
		File fullTextIndexPath = createPath(basePath, "text");
		File transactionLogPath = createPath(basePath, "transactions");
		File logStorePath = createPath(basePath, "logs");
		File appDataPath = createPath(basePath, "apps");
		File embeddedContentStorePath = createPath(basePath, "embedded");
		File webserverStaticFilesPath = createPath(basePath, "static");
		File temp = createPath(basePath, "temp");
		return new ApplicationServerConfig(indexPath, fullTextIndexPath, transactionLogPath, fileStorePath, logStorePath, appDataPath, embeddedContentStorePath, webserverStaticFilesPath, temp, teamAppsConfiguration, port);
	}

	public ApplicationServerConfig(File indexPath, File fullTextIndexPath, File transactionLogPath, File fileStorePath, File logStorePath, File appDataPath, File embeddedContentStorePath, File webserverStaticFilesPath, File tempPath, TeamAppsConfiguration teamAppsConfiguration, int port) {
		this.indexPath = indexPath;
		this.fullTextIndexPath = fullTextIndexPath;
		this.transactionLogPath = transactionLogPath;
		this.fileStorePath = fileStorePath;
		this.logStorePath = logStorePath;
		this.appDataPath = appDataPath;
		this.embeddedContentStorePath = embeddedContentStorePath;
		this.webserverStaticFilesPath = webserverStaticFilesPath;
		this.tempPath = tempPath;
		this.teamAppsConfiguration = teamAppsConfiguration;
		this.port = port;
	}


	private static File createPath(File basePath, String name) {
		File path = new File(basePath, name);
		path.mkdir();
		return path;
	}

	public File getIndexPath() {
		return indexPath;
	}

	public File getFullTextIndexPath() {
		return fullTextIndexPath;
	}

	public File getTransactionLogPath() {
		return transactionLogPath;
	}

	public File getAppDataPath() {
		return appDataPath;
	}

	public File getFileStorePath() {
		return fileStorePath;
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

	public File getTempPath() {
		return tempPath;
	}

	public TeamAppsConfiguration getTeamAppsConfiguration() {
		return teamAppsConfiguration;
	}

	public int getPort() {
		return port;
	}
}
