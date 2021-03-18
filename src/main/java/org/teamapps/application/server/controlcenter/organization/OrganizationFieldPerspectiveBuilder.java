package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class OrganizationFieldPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public OrganizationFieldPerspectiveBuilder() {
		super("organizationFieldPerspective", ApplicationIcons.ELEMENTS_TREE, "organizationField.title", "organizationField.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new OrganizationFieldPerspective(applicationInstanceData, mutableValue);
	}
}
