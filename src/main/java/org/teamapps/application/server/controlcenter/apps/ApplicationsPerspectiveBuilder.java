package org.teamapps.application.server.controlcenter.apps;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;

public class ApplicationsPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public ApplicationsPerspectiveBuilder() {
		super("applications", ApplicationIcons.WINDOW, "applications.title", "applications.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new ApplicationsPerspective(applicationInstanceData, mutableValue);
	}
}
