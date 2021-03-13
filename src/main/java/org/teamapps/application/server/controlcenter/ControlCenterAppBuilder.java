package org.teamapps.application.server.controlcenter;

import org.teamapps.application.api.application.ApplicationPerspectiveBuilder;
import org.teamapps.application.api.application.PerspectiveBuilder;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.application.server.controlcenter.accesscontrol.AccessControlPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.applications.ApplicationsPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.database.DataBasePerspectiveBuilder;
import org.teamapps.application.server.controlcenter.organization.OrganizationPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.roles.RolesPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.systemlog.SystemLogPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.systenconfig.SystemConfigurationPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.translations.TranslationsPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.users.UsersPerspectiveBuilder;
import org.teamapps.icons.Icon;
import org.teamapps.model.ControlCenterSchema;
import org.teamapps.universaldb.schema.SchemaInfoProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ControlCenterAppBuilder implements ApplicationPerspectiveBuilder {

	@Override
	public List<PerspectiveBuilder> getPerspectiveBuilders() {
		return Arrays.asList(
				new UsersPerspectiveBuilder(),
				new OrganizationPerspectiveBuilder(),
				new RolesPerspectiveBuilder(),
				new AccessControlPerspectiveBuilder(),
				new ApplicationsPerspectiveBuilder(),
				new TranslationsPerspectiveBuilder(),
				new SystemLogPerspectiveBuilder(),
				new DataBasePerspectiveBuilder(),
				new SystemConfigurationPerspectiveBuilder()
		);
	}

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(0, 11);
	}

	@Override
	public Icon getApplicationIcon() {
		return ApplicationIcons.WINDOW_KEY;
	}

	@Override
	public String getApplicationName() {
		return "controlCenter";
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
		return null;
	}

	@Override
	public List<PrivilegeGroup> getPrivilegeGroups() {
		return Privileges.getPrivileges();
	}

	@Override
	public LocalizationData getLocalizationData() {
		return LocalizationData.createFromPropertyFiles("org.teamapps.application.server.i18n.controlCenter", getClass().getClassLoader(), Locale.ENGLISH);
	}

	@Override
	public SchemaInfoProvider getDatabaseModel() {
		return new ControlCenterSchema();
		//return null;
	}

	@Override
	public ApplicationConfig getApplicationConfig() {
		return null;
	}

	@Override
	public void bootstrapApplicationBuilder() {

	}

	@Override
	public boolean isApplicationAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return true;
	}
}
