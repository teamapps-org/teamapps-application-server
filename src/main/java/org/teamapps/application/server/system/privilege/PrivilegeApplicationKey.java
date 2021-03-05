package org.teamapps.application.server.system.privilege;

import org.teamapps.application.api.organization.OrgField;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.model.controlcenter.*;

import java.util.Objects;

public class PrivilegeApplicationKey {

	public static PrivilegeApplicationKey create(ManagedApplicationPerspective perspective) {
		OrganizationField organizationField = perspective.getManagedApplication().getOrganizationField();
		OrgField orgField = OrganizationUtils.convert(organizationField);
		Application application = perspective.getApplicationPerspective().getApplication();
		if (application == null) {
			return null;
		}
		return new PrivilegeApplicationKey(application, orgField);
	}

	public static PrivilegeApplicationKey createUnmanagedKey(ManagedApplication managedApplication) {
		return new PrivilegeApplicationKey(managedApplication.getMainApplication(), null);
	}

	public static PrivilegeApplicationKey create(ManagedApplication managedApplication) {
		return new PrivilegeApplicationKey(managedApplication.getMainApplication(), null);
	}


	public static PrivilegeApplicationKey create(RoleApplicationRoleAssignment roleApplicationRoleAssignment) {
		return new PrivilegeApplicationKey(roleApplicationRoleAssignment.getApplication(), OrganizationUtils.convert(roleApplicationRoleAssignment.getOrganizationFieldFilter()));
	}

	public static PrivilegeApplicationKey create(RolePrivilegeAssignment privilegeAssignment) {
		return new PrivilegeApplicationKey(privilegeAssignment.getApplication(), OrganizationUtils.convert(privilegeAssignment.getOrganizationFieldFilter()));
	}

	private final String key;
	private final Application application;
	private final OrgField orgField;

	private PrivilegeApplicationKey(Application application, OrgField orgField) {
		this.application = application;
		this.orgField = orgField;
		this.key = orgField != null ? application.getId() + "-" + orgField.getId() : application.getId() + "";
	}

	public Application getApplication() {
		return application;
	}

	public OrgField getOrgField() {
		return orgField;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PrivilegeApplicationKey that = (PrivilegeApplicationKey) o;
		return key.equals(that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}
}
