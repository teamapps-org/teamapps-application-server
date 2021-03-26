package org.teamapps.application.server.controlcenter.dbexplorer;

import org.teamapps.application.api.application.ApplicationBuilder;
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
import org.teamapps.icons.Icon;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.schema.SchemaInfoProvider;
import org.teamapps.ux.application.ResponsiveApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseExplorerAppBuilder implements ApplicationBuilder {

	private final UniversalDB universalDB;

	public DatabaseExplorerAppBuilder(UniversalDB universalDB) {
		this.universalDB = universalDB;
	}

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(0, 1);
	}

	@Override
	public Icon getApplicationIcon() {
		return ApplicationIcons.DATA;
	}

	@Override
	public String getApplicationName() {
		return "databaseExplorer";
	}

	@Override
	public String getApplicationTitleKey() {
		return Dictionary.DATABASE;
	}

	@Override
	public String getApplicationDescriptionKey() {
		return Dictionary.APPLICATION_LAUNCHER;
	}

	@Override
	public List<ApplicationRole> getApplicationRoles() {
		return null;
	}

	@Override
	public List<PrivilegeGroup> getPrivilegeGroups() {
		return Collections.emptyList(); //DatabasePrivileges.getPrivileges();
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
	public void bootstrapApplicationBuilder() {

	}

	@Override
	public boolean isApplicationAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public void build(ResponsiveApplication responsiveApplication, ApplicationInstanceData applicationInstanceData) {
		new DatabaseExplorerApplication(responsiveApplication, applicationInstanceData, universalDB);
	}
}
