package org.teamapps.application.server.system.privilege;

import org.teamapps.application.api.organization.OrgUnit;
import org.teamapps.application.api.privilege.*;
import org.teamapps.model.controlcenter.*;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.utils.RoleUtils;
import org.teamapps.application.server.system.utils.ValueConverterUtils;

import java.util.*;

public class UserPrivileges {

	private final User user;
	private final SystemRegistry systemRegistry;

	private final Map<PrivilegeApplicationKey, Set<SimplePrivilege>> simplePrivilegesMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, Map<SimpleOrganizationalPrivilege, Set<OrgUnit>>> simpleOrganizationPrivilegeMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, Map<SimpleCustomObjectPrivilege, Set<PrivilegeObject>>> simpleCustomObjectPrivilegeMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, Map<StandardPrivilegeGroup, Set<Privilege>>> standardPrivilegeMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, Map<OrganizationalPrivilegeGroup, Map<Privilege, Set<OrgUnit>>>> organizationPrivilegeGroupMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, Map<CustomObjectPrivilegeGroup, Map<Privilege, Set<PrivilegeObject>>>> customObjectPrivilegeGroupMap = new HashMap<>();
	private final Map<PrivilegeApplicationKey, UserApplicationPrivilege> userApplicationPrivilegeByApplication = new HashMap<>();

	public UserPrivileges(User user, SystemRegistry systemRegistry) {
		this.user = user;
		this.systemRegistry = systemRegistry;
		calculatePrivileges();
	}

	private void calculatePrivileges() {
		for (UserRoleAssignment roleAssignment : user.getRoleAssignments()) {
			Role role = roleAssignment.getRole();
			OrganizationUnit organizationUnit = roleAssignment.getOrganizationUnit();
			Set<Role> privilegeRoles = RoleUtils.getAllPrivilegeRoles(role);
			for (Role privilegeRole : privilegeRoles) {
				for (RoleApplicationRoleAssignment roleApplicationRoleAssignment : privilegeRole.getApplicationRoleAssignments()) {
					calculatePrivilegesFromApplicationRoleAssignment(organizationUnit, roleApplicationRoleAssignment);
				}

				for (RolePrivilegeAssignment privilegeAssignment : privilegeRole.getPrivilegeAssignments()) {
					calculatePrivilegesFromRolePrivilegeAssignment(organizationUnit, privilegeAssignment);
				}
			}
		}
	}

	private void calculatePrivilegesFromApplicationRoleAssignment(OrganizationUnit organizationUnit, RoleApplicationRoleAssignment roleApplicationRoleAssignment) {
		try {
			Application application = roleApplicationRoleAssignment.getApplication();
			String applicationRoleName = roleApplicationRoleAssignment.getApplicationRoleName();
			PrivilegeApplicationKey privilegeApplicationKey = PrivilegeApplicationKey.create(roleApplicationRoleAssignment);
			OrganizationUnit fixedOrganizationRoot = roleApplicationRoleAssignment.getFixedOrganizationRoot();
			List<OrganizationUnitType> organizationUnitTypeFilter = roleApplicationRoleAssignment.getOrganizationUnitTypeFilter();
			LoadedApplication loadedApplication = systemRegistry.getLoadedApplication(application);
			if (loadedApplication != null) {
				ApplicationRole applicationRole = loadedApplication.getAppPrivilegeProvider().getApplicationRole(applicationRoleName);
				if (applicationRole != null && applicationRole.getPrivilegeGroups() != null) {
					Set<OrganizationUnit> allUnits = OrganizationUtils.getAllUnits(fixedOrganizationRoot != null ? fixedOrganizationRoot : organizationUnit, organizationUnitTypeFilter);
					List<OrgUnit> orgUnits = OrganizationUtils.convertList(allUnits);
					List<PrivilegeGroup> privilegeGroups = applicationRole.getPrivilegeGroups();
					for (PrivilegeGroup privilegeGroup : privilegeGroups) {
						switch (privilegeGroup.getType()) {
							case SIMPLE_PRIVILEGE:
								SimplePrivilege simplePrivilege = (SimplePrivilege) privilegeGroup;
								simplePrivilegesMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashSet<>())
										.add(simplePrivilege);
								break;
							case SIMPLE_ORGANIZATIONAL_PRIVILEGE:
								SimpleOrganizationalPrivilege simpleOrganizationalPrivilege = (SimpleOrganizationalPrivilege) privilegeGroup;
								simpleOrganizationPrivilegeMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
										.computeIfAbsent(simpleOrganizationalPrivilege, s -> new HashSet<>())
										.addAll(orgUnits);
								break;
							case SIMPLE_CUSTOM_OBJECT_PRIVILEGE:
								SimpleCustomObjectPrivilege simpleCustomObjectPrivilege = (SimpleCustomObjectPrivilege) privilegeGroup;
								List<PrivilegeObject> privilegeObjects = simpleCustomObjectPrivilege.getPrivilegeObjectsSupplier().get();
								simpleCustomObjectPrivilegeMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
										.computeIfAbsent(simpleCustomObjectPrivilege, s -> new HashSet<>())
										.addAll(privilegeObjects);
								break;
							case STANDARD_PRIVILEGE_GROUP:
								StandardPrivilegeGroup standardPrivilegeGroup = (StandardPrivilegeGroup) privilegeGroup;
								List<Privilege> privileges = privilegeGroup.getPrivileges();
								standardPrivilegeMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
										.computeIfAbsent(standardPrivilegeGroup, s -> new HashSet<>())
										.addAll(privileges);
								break;
							case ORGANIZATIONAL_PRIVILEGE_GROUP:
								OrganizationalPrivilegeGroup organizationalPrivilegeGroup = (OrganizationalPrivilegeGroup) privilegeGroup;
								List<Privilege> groupPrivileges = privilegeGroup.getPrivileges();
								Map<Privilege, Set<OrgUnit>> orgUnitsByPrivilege = organizationPrivilegeGroupMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
										.computeIfAbsent(organizationalPrivilegeGroup, s -> new HashMap<>());
								for (Privilege privilege : groupPrivileges) {
									orgUnitsByPrivilege
											.computeIfAbsent(privilege, p -> new HashSet<>())
											.addAll(orgUnits);
								}
								break;
							case CUSTOM_OBJECT_PRIVILEGE_GROUP:
								CustomObjectPrivilegeGroup customObjectPrivilegeGroup = (CustomObjectPrivilegeGroup) privilegeGroup;
								List<PrivilegeObject> customPrivileges = customObjectPrivilegeGroup.getPrivilegeObjectsSupplier().get();
								List<Privilege> customObjectPrivileges = customObjectPrivilegeGroup.getPrivileges();
								Map<Privilege, Set<PrivilegeObject>> customObjectsByPrivilege = customObjectPrivilegeGroupMap
										.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
										.computeIfAbsent(customObjectPrivilegeGroup, c -> new HashMap<>());
								for (Privilege privilege : customObjectPrivileges) {
									customObjectsByPrivilege
											.computeIfAbsent(privilege, p -> new HashSet<>())
											.addAll(customPrivileges);
								}
								break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void calculatePrivilegesFromRolePrivilegeAssignment(OrganizationUnit organizationUnit, RolePrivilegeAssignment privilegeAssignment) {
		Application application = privilegeAssignment.getApplication();
		LoadedApplication loadedApplication = systemRegistry.getLoadedApplication(application);
		if (loadedApplication != null) {
			ApplicationScopePrivilegeProvider privilegeProvider = loadedApplication.getAppPrivilegeProvider();
			PrivilegeApplicationKey privilegeApplicationKey = PrivilegeApplicationKey.create(privilegeAssignment);
			OrganizationUnit fixedOrganizationRoot = privilegeAssignment.getFixedOrganizationRoot();
			List<OrganizationUnitType> organizationUnitTypeFilter = privilegeAssignment.getOrganizationUnitTypeFilter();
			PrivilegeGroup privilegeGroup = privilegeProvider.getPrivilegeGroup(privilegeAssignment.getPrivilegeGroup());
			List<Privilege> privileges = privilegeProvider.getPrivilegesByNameList(ValueConverterUtils.decompressToStringList(privilegeAssignment.getPrivileges()));
			boolean privilegeObjectInheritance = privilegeAssignment.getPrivilegeObjectInheritance();
			List<Integer> privilegeObjectIdList = ValueConverterUtils.decompressIds(privilegeAssignment.getPrivilegeObjects());
			List<PrivilegeObject> privilegeObjects = privilegeProvider.getPrivilegeObjects(privilegeGroup, privilegeObjectIdList, privilegeObjectInheritance);
			Set<OrganizationUnit> allUnits = OrganizationUtils.getAllUnits(fixedOrganizationRoot != null ? fixedOrganizationRoot : organizationUnit, organizationUnitTypeFilter);
			List<OrgUnit> orgUnits = OrganizationUtils.convertList(allUnits);
			try {
				switch (privilegeGroup.getType()) {
					case SIMPLE_PRIVILEGE:
						SimplePrivilege simplePrivilege = (SimplePrivilege) privilegeGroup;
						simplePrivilegesMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashSet<>())
								.add(simplePrivilege);
						break;
					case SIMPLE_ORGANIZATIONAL_PRIVILEGE:
						SimpleOrganizationalPrivilege simpleOrganizationalPrivilege = (SimpleOrganizationalPrivilege) privilegeGroup;
						simpleOrganizationPrivilegeMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
								.computeIfAbsent(simpleOrganizationalPrivilege, s -> new HashSet<>())
								.addAll(orgUnits);
						break;
					case SIMPLE_CUSTOM_OBJECT_PRIVILEGE:
						SimpleCustomObjectPrivilege simpleCustomObjectPrivilege = (SimpleCustomObjectPrivilege) privilegeGroup;
						simpleCustomObjectPrivilegeMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
								.computeIfAbsent(simpleCustomObjectPrivilege, s -> new HashSet<>())
								.addAll(privilegeObjects);
						break;
					case STANDARD_PRIVILEGE_GROUP:
						StandardPrivilegeGroup standardPrivilegeGroup = (StandardPrivilegeGroup) privilegeGroup;
						standardPrivilegeMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
								.computeIfAbsent(standardPrivilegeGroup, s -> new HashSet<>())
								.addAll(privileges);
						break;
					case ORGANIZATIONAL_PRIVILEGE_GROUP:
						OrganizationalPrivilegeGroup organizationalPrivilegeGroup = (OrganizationalPrivilegeGroup) privilegeGroup;
						Map<Privilege, Set<OrgUnit>> orgUnitsByPrivilege = organizationPrivilegeGroupMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
								.computeIfAbsent(organizationalPrivilegeGroup, s -> new HashMap<>());
						for (Privilege privilege : privileges) {
							orgUnitsByPrivilege
									.computeIfAbsent(privilege, p -> new HashSet<>())
									.addAll(orgUnits);
						}
						break;
					case CUSTOM_OBJECT_PRIVILEGE_GROUP:
						CustomObjectPrivilegeGroup customObjectPrivilegeGroup = (CustomObjectPrivilegeGroup) privilegeGroup;
						Map<Privilege, Set<PrivilegeObject>> customObjectsByPrivilege = customObjectPrivilegeGroupMap
								.computeIfAbsent(privilegeApplicationKey, app -> new HashMap<>())
								.computeIfAbsent(customObjectPrivilegeGroup, c -> new HashMap<>());
						for (Privilege privilege : privileges) {
							customObjectsByPrivilege
									.computeIfAbsent(privilege, p -> new HashSet<>())
									.addAll(privilegeObjects);
						}
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ApplicationPrivilegeProvider getApplicationPrivilegeProvider(PrivilegeApplicationKey privilegeApplicationKey) {
		UserApplicationPrivilege userApplicationPrivilege = userApplicationPrivilegeByApplication.get(privilegeApplicationKey);
		if (userApplicationPrivilege == null) {
			userApplicationPrivilege = new UserApplicationPrivilege(this, privilegeApplicationKey);
			userApplicationPrivilegeByApplication.put(privilegeApplicationKey, userApplicationPrivilege);
		}
		return userApplicationPrivilege;
	}

	public Map<PrivilegeApplicationKey, Set<SimplePrivilege>> getSimplePrivilegesMap() {
		return simplePrivilegesMap;
	}

	public Map<PrivilegeApplicationKey, Map<SimpleOrganizationalPrivilege, Set<OrgUnit>>> getSimpleOrganizationPrivilegeMap() {
		return simpleOrganizationPrivilegeMap;
	}

	public Map<PrivilegeApplicationKey, Map<SimpleCustomObjectPrivilege, Set<PrivilegeObject>>> getSimpleCustomObjectPrivilegeMap() {
		return simpleCustomObjectPrivilegeMap;
	}

	public Map<PrivilegeApplicationKey, Map<StandardPrivilegeGroup, Set<Privilege>>> getStandardPrivilegeMap() {
		return standardPrivilegeMap;
	}

	public Map<PrivilegeApplicationKey, Map<OrganizationalPrivilegeGroup, Map<Privilege, Set<OrgUnit>>>> getOrganizationPrivilegeGroupMap() {
		return organizationPrivilegeGroupMap;
	}

	public Map<PrivilegeApplicationKey, Map<CustomObjectPrivilegeGroup, Map<Privilege, Set<PrivilegeObject>>>> getCustomObjectPrivilegeGroupMap() {
		return customObjectPrivilegeGroupMap;
	}

}
