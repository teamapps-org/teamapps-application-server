package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class ApplicationProvisioningPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public ApplicationProvisioningPerspectiveBuilder() {
		super("applicationProvisioning", ApplicationIcons.INSTALL, "applicationProvisioning.title", "applicationProvisioning.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new ApplicationProvisioningPerspective(applicationInstanceData, mutableValue);
	}
}
