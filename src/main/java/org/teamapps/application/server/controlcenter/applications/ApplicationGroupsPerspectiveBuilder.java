package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class ApplicationGroupsPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public ApplicationGroupsPerspectiveBuilder() {
		super("applicationGroups", ApplicationIcons.WINDOWS, "applicationGroups.title", "applicationGroups.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new ApplicationGroupsPerspective(applicationInstanceData, mutableValue);
	}
}
