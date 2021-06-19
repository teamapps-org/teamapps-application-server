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
package org.teamapps.application.server.controlcenter.dbexplorer;

import org.teamapps.application.api.application.AbstractBaseApplicationBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.localization.LocalizationEntry;
import org.teamapps.application.api.localization.LocalizationEntrySet;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.application.server.controlcenter.Privileges;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.schema.SchemaInfoProvider;
import org.teamapps.ux.application.ResponsiveApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseExplorerAppBuilder extends AbstractBaseApplicationBuilder {

	private final UniversalDB universalDB;

	public DatabaseExplorerAppBuilder(UniversalDB universalDB) {
		super("databaseExplorer", ApplicationIcons.DATA, Dictionary.DATABASE, Dictionary.APPLICATION_LAUNCHER);
		this.universalDB = universalDB;
	}

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(0, 1);
	}

	@Override
	public List<ApplicationRole> getApplicationRoles() {
		return Collections.emptyList();
	}

	@Override
	public List<PrivilegeGroup> getPrivilegeGroups() {
		return Collections.emptyList();
	}

	@Override
	public LocalizationData getLocalizationData() {
		return () -> Arrays.asList(new LocalizationEntrySet() {
			@Override
			public String getLanguage() {
				return "en";
			}

			@Override
			public List<LocalizationEntry> getEntries() {
				return Collections.emptyList();
			}

			@Override
			public boolean isMachineTranslation() {
				return false;
			}
		});
	}

	@Override
	public SchemaInfoProvider getDatabaseModel() {
		return null;
	}

	@Override
	public ApplicationConfig getApplicationConfig() {
		return null;
	}
	
	@Override
	public boolean isApplicationAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return privilegeProvider.isAllowed(Privileges.LAUNCH_APPLICATION);
	}

	@Override
	public void build(ResponsiveApplication responsiveApplication, ApplicationInstanceData applicationInstanceData) {
		new DatabaseExplorerApplication(responsiveApplication, applicationInstanceData, universalDB);
	}
}
