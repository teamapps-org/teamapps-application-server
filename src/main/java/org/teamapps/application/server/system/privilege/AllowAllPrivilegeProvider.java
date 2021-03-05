package org.teamapps.application.server.system.privilege;

import org.teamapps.application.api.organization.OrgUnit;
import org.teamapps.application.api.privilege.*;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.model.controlcenter.OrganizationUnit;

import java.util.List;

public class AllowAllPrivilegeProvider implements ApplicationPrivilegeProvider {
	@Override
	public boolean isAllowed(SimplePrivilege simplePrivilege) {
		return true;
	}

	@Override
	public boolean isAllowed(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege, OrgUnit orgUnit) {
		return true;
	}

	@Override
	public boolean isAllowed(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege, PrivilegeObject privilegeObject) {
		return true;
	}

	@Override
	public boolean isAllowed(StandardPrivilegeGroup standardPrivilegeGroup, Privilege privilege) {
		return true;
	}

	@Override
	public boolean isAllowed(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege, OrgUnit orgUnit) {
		return true;
	}

	@Override
	public boolean isAllowed(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege, PrivilegeObject privilegeObject) {
		return true;
	}

	@Override
	public List<OrgUnit> getAllowedUnits(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege) {
		return OrganizationUtils.convertList(OrganizationUnit.getAll());
	}

	@Override
	public List<OrgUnit> getAllowedUnits(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege) {
		return OrganizationUtils.convertList(OrganizationUnit.getAll());
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege) {
		return simpleCustomObjectPrivilege.getPrivilegeObjectsSupplier().get();
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege) {
		return customObjectPrivilegeGroup.getPrivilegeObjectsSupplier().get();
	}
}
