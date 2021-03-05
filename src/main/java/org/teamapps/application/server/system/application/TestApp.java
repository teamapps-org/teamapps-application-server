package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.AbstractApplication;
import org.teamapps.application.api.application.Application;
import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.icons.Icon;
import org.teamapps.universaldb.schema.SchemaInfoProvider;
import org.teamapps.ux.application.ResponsiveApplication;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.dummy.DummyComponent;
import org.teamapps.ux.component.panel.Panel;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TestApp implements ApplicationBuilder {

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(1, 0);
	}

	@Override
	public Icon getApplicationIcon() {
		return ApplicationIcons.LANTERN_ON;
	}

	@Override
	public String getApplicationName() {
		return "testApp";
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
	public void build(ResponsiveApplication responsiveApplication, ApplicationInstanceData applicationInstanceData) {
		Perspective perspective = Perspective.createPerspective();
		perspective.addView(View.createView(StandardLayout.CENTER, ApplicationIcons.ADDRESS_BOOK, "Test view", new Panel(ApplicationIcons.WINDOW, "test 1")));
		perspective.addView(View.createView(StandardLayout.RIGHT_BOTTOM, ApplicationIcons.WAX_SEAL_BROKEN, "Test view other", new DummyComponent()));
		responsiveApplication.showPerspective(perspective);
	}


}
