package org.teamapps.application.server.system.privilege;

import org.teamapps.application.api.organization.OrgUnit;
import org.teamapps.application.api.privilege.*;

import java.util.*;

public class UserApplicationPrivilege implements ApplicationPrivilegeProvider {

	private final UserPrivileges userPrivileges;
	private final PrivilegeApplicationKey privilegeApplicationKey;
	private Set<SimplePrivilege> simplePrivileges;
	private Map<SimpleOrganizationalPrivilege, Set<OrgUnit>> simpleOrganizationalPrivilegeSetMap;
	private Map<SimpleCustomObjectPrivilege, Set<PrivilegeObject>> simpleCustomObjectPrivilegeSetMap;
	private Map<StandardPrivilegeGroup, Set<Privilege>> standardPrivilegeGroupSetMap;
	private Map<OrganizationalPrivilegeGroup, Map<Privilege, Set<OrgUnit>>> organizationalPrivilegeGroupMap;
	private Map<CustomObjectPrivilegeGroup, Map<Privilege, Set<PrivilegeObject>>> customObjectPrivilegeGroupMap;

	public UserApplicationPrivilege(UserPrivileges userPrivileges, PrivilegeApplicationKey privilegeApplicationKey) {
		this.userPrivileges = userPrivileges;
		this.privilegeApplicationKey = privilegeApplicationKey;
		init();
	}

	private void init() {
		simplePrivileges = userPrivileges.getSimplePrivilegesMap().get(privilegeApplicationKey);
		simpleOrganizationalPrivilegeSetMap = userPrivileges.getSimpleOrganizationPrivilegeMap().get(privilegeApplicationKey);
		simpleCustomObjectPrivilegeSetMap = userPrivileges.getSimpleCustomObjectPrivilegeMap().get(privilegeApplicationKey);
		standardPrivilegeGroupSetMap = userPrivileges.getStandardPrivilegeMap().get(privilegeApplicationKey);
		organizationalPrivilegeGroupMap = userPrivileges.getOrganizationPrivilegeGroupMap().get(privilegeApplicationKey);
		customObjectPrivilegeGroupMap = userPrivileges.getCustomObjectPrivilegeGroupMap().get(privilegeApplicationKey);
	}

	@Override
	public boolean isAllowed(SimplePrivilege simplePrivilege) {
		if (simplePrivileges == null) {
			return false;
		} else {
			return simplePrivileges.contains(simplePrivilege);
		}
	}

	@Override
	public boolean isAllowed(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege, OrgUnit orgUnit) {
		if (simpleOrganizationalPrivilegeSetMap == null || !simpleOrganizationalPrivilegeSetMap.containsKey(simpleOrganizationalPrivilege)) {
			return false;
		} else {
			return simpleOrganizationalPrivilegeSetMap.get(simpleOrganizationalPrivilege).contains(orgUnit);
		}
	}

	@Override
	public boolean isAllowed(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege, PrivilegeObject privilegeObject) {
		if (simpleCustomObjectPrivilegeSetMap == null || !simpleCustomObjectPrivilegeSetMap.containsKey(simpleCustomObjectPrivilege)) {
			return false;
		} else {
			return simpleCustomObjectPrivilegeSetMap.get(simpleCustomObjectPrivilege).contains(privilegeObject);
		}
	}

	@Override
	public boolean isAllowed(StandardPrivilegeGroup standardPrivilegeGroup, Privilege privilege) {
		if (standardPrivilegeGroupSetMap == null || !standardPrivilegeGroupSetMap.containsKey(standardPrivilegeGroup)) {
			return false;
		} else {
			return standardPrivilegeGroupSetMap.get(standardPrivilegeGroup).contains(privilege);
		}
	}

	@Override
	public boolean isAllowed(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege, OrgUnit orgUnit) {
		if (organizationalPrivilegeGroupMap == null || !organizationalPrivilegeGroupMap.containsKey(organizationalPrivilegeGroup)) {
			return false;
		} else {
			Set<OrgUnit> orgUnits = organizationalPrivilegeGroupMap.get(organizationalPrivilegeGroup).get(privilege);
			if (orgUnits != null && orgUnits.contains(orgUnit)) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean isAllowed(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege, PrivilegeObject privilegeObject) {
		if (customObjectPrivilegeGroupMap == null || !customObjectPrivilegeGroupMap.containsKey(customObjectPrivilegeGroup)) {
			return false;
		} else {
			Set<PrivilegeObject> privilegeObjects = customObjectPrivilegeGroupMap.get(customObjectPrivilegeGroup).get(privilege);
			if (privilegeObjects != null && privilegeObjects.contains(privilegeObject)) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public List<OrgUnit> getAllowedUnits(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege) {
		if (simpleOrganizationalPrivilegeSetMap == null || !simpleOrganizationalPrivilegeSetMap.containsKey(simpleOrganizationalPrivilege)) {
			return Collections.emptyList();
		} else {
			return new ArrayList<>(simpleOrganizationalPrivilegeSetMap.get(simpleOrganizationalPrivilege));
		}
	}

	@Override
	public List<OrgUnit> getAllowedUnits(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege) {
		if (organizationalPrivilegeGroupMap == null || !organizationalPrivilegeGroupMap.containsKey(organizationalPrivilegeGroup)) {
			return Collections.emptyList();
		} else {
			Set<OrgUnit> orgUnits = organizationalPrivilegeGroupMap.get(organizationalPrivilegeGroup).get(privilege);
			return new ArrayList<>(orgUnits);
		}
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege) {
		if (simpleCustomObjectPrivilegeSetMap == null || !simpleCustomObjectPrivilegeSetMap.containsKey(simpleCustomObjectPrivilege)) {
			return Collections.emptyList();
		} else {
			return new ArrayList<>(simpleCustomObjectPrivilegeSetMap.get(simpleCustomObjectPrivilege));
		}
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege) {
		if (customObjectPrivilegeGroupMap == null || !customObjectPrivilegeGroupMap.containsKey(customObjectPrivilegeGroup)) {
			return Collections.emptyList();
		} else {
			Set<PrivilegeObject> privilegeObjects = customObjectPrivilegeGroupMap.get(customObjectPrivilegeGroup).get(privilege);
			return new ArrayList<>(privilegeObjects);
		}
	}
}
