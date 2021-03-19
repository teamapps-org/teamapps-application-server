package org.teamapps.application.server.controlcenter.roles;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class UserRoleAssignmentPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public UserRoleAssignmentPerspectiveBuilder() {
		super("userRoleAssignmentPerspective", ApplicationIcons.USERS_THREE_RELATION, "userRoleAssignment.title", "userRoleAssignment.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new UserRoleAssignmentPerspective(applicationInstanceData, mutableValue);
	}
}