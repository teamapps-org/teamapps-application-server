package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.*;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.icons.Icon;
import org.teamapps.universaldb.schema.SchemaInfoProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TestApp2 implements ApplicationPerspectiveBuilder {

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(1, 0);
	}

	@Override
	public Icon getApplicationIcon() {
		return ApplicationIcons.WINDOW;
	}

	@Override
	public String getApplicationName() {
		return "testApp2";
	}

	@Override
	public String getApplicationTitleKey() {
		return "application.title";
	}

	@Override
	public String getApplicationDescriptionKey() {
		return "application.desc";
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
		return LocalizationData.createFromPropertyFiles("org.teamapps.application.server.i18n.testApp", getClass().getClassLoader(), Locale.ENGLISH);
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
	public List<PerspectiveBuilder> getPerspectiveBuilders() {
		return Arrays.asList(new Perspective1(), new Perspective2());
	}
}
