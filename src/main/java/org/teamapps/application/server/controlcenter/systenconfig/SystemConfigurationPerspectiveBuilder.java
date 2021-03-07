package org.teamapps.application.server.controlcenter.systenconfig;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class SystemConfigurationPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public SystemConfigurationPerspectiveBuilder() {
		super("systemConfigurationPerspective", ApplicationIcons.RACK_SERVERS, "systemConfiguration.title", "systemConfiguration.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return null;
	}
}
