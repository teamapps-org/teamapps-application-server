package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class OrganizationUnitTypePerspectiveBuilder extends AbstractPerspectiveBuilder {

	public OrganizationUnitTypePerspectiveBuilder() {
		super("organizationUnitTypePerspective", ApplicationIcons.ELEMENTS_HIERARCHY, "organizationUnitType.title", "organizationUnitType.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new OrganizationUnitTypePerspective(applicationInstanceData, mutableValue);
	}
}
