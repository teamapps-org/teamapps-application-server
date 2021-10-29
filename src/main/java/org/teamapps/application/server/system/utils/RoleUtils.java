/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.system.utils;

import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.model.controlcenter.*;

import java.util.*;
import java.util.stream.Collectors;

public class RoleUtils {

	public static Set<Role> getAllRoleInstances(Role role) {
		Set<Role> roleSet = new HashSet<>();
		calculateRoleInstances(role, roleSet);
		return roleSet;
	}

	private static void calculateRoleInstances(Role role, Set<Role> roleSet) {
		if (!roleSet.contains(role)) {
			roleSet.add(role);
			for (Role specializationRole : role.getSpecializationRoles()) {
				calculateRoleInstances(specializationRole, roleSet);
			}
		}
	}

	public static Set<Role> getAllPrivilegeRoles(Role role) {
		Set<Role> roleSet = new HashSet<>();
		calculatePrivilegeRoles(role, roleSet);
		return roleSet;
	}

	private static void calculatePrivilegeRoles(Role role, Set<Role> roleSet) {
		if (!roleSet.contains(role)) {
			roleSet.add(role);
			for (Role generalizationRole : role.getGeneralizationRoles()) {
				calculatePrivilegeRoles(generalizationRole, roleSet);
			}
			for (Role privilegesSendingRole : role.getPrivilegesSendingRoles()) {
				calculatePrivilegeRoles(privilegesSendingRole, roleSet);
			}
		}
	}

	public static List<UserRoleAssignment> getUserRoleAssignments(Role role, boolean withSpecializationRoles, OrganizationUnit organizationUnit, List<OrganizationUnitType> organizationUnitTypesFilter) {
		if (role == null || organizationUnit == null) {
			return Collections.emptyList();
		}
		Set<Role> roleSet = withSpecializationRoles ? getAllRoleInstances(role) : new HashSet<>(Collections.singletonList(role));
		Set<OrganizationUnit> allUnits = OrganizationUtils.getAllUnits(organizationUnit, organizationUnitTypesFilter);
		return getUserRoleAssignments(roleSet, allUnits);
	}

	public static List<UserRoleAssignment> getUserRoleAssignments(Set<Role> roleSet, Set<OrganizationUnit> organizationUnits) {
		return UserRoleAssignment.getAll().stream()
				.filter(assignment -> roleSet.contains(assignment.getRole()))
				.filter(assignment -> organizationUnits.contains(assignment.getOrganizationUnit()))
				.collect(Collectors.toList());
	}

	public static int getMemberCount(Role role, boolean withSpecializationRoles) {
		Set<Role> roleSet = withSpecializationRoles ? getAllRoleInstances(role) : new HashSet<>(Collections.singletonList(role));
		return (int) UserRoleAssignment.getAll().stream()
				.filter(assignment -> roleSet.contains(assignment.getRole()))
				.count();
	}

	public static List<UserRoleAssignment> getMembers(Role role, boolean withSpecializationRoles) {
		Set<Role> roleSet = withSpecializationRoles ? getAllRoleInstances(role) : new HashSet<>(Collections.singletonList(role));
		return UserRoleAssignment.getAll().stream()
				.filter(assignment -> roleSet.contains(assignment.getRole()))
				.collect(Collectors.toList());
	}

	public static List<ApplicationPrivilegeGroup> calculateRolePrivileges(Role role, SystemRegistry systemRegistry) {
		Map<String, ApplicationRole> applicationRoleMap = new HashMap<>();
		for (LoadedApplication loadedApplication : systemRegistry.getLoadedApplications()) {
			String name = loadedApplication.getApplication().getName();
			List<ApplicationRole> applicationRoles = loadedApplication.getBaseApplicationBuilder().getApplicationRoles();
			if (applicationRoles != null) {
				for (ApplicationRole applicationRole : applicationRoles) {
					applicationRoleMap.put(name + "." + applicationRole.getName(), applicationRole);
				}
			}
		}
		Map<String, ApplicationPrivilegeGroup> groupMap = new HashMap<>();
		ApplicationPrivilegeGroup.getAll().forEach(group -> groupMap.put(group.getApplication().getName() + "." + group.getName(), group));

		Set<Role> privilegeRoles = getAllPrivilegeRoles(role);
		for (Role privilegeRole : privilegeRoles) {
			for (RoleApplicationRoleAssignment roleApplicationRoleAssignment : privilegeRole.getApplicationRoleAssignments()) {
				String applicationRoleName = roleApplicationRoleAssignment.getApplicationRoleName();
				String applicationName = roleApplicationRoleAssignment.getApplication().getName();
				ApplicationRole applicationRole = applicationRoleMap.get(applicationName + "." + applicationRoleName);
				if (applicationRole != null) {
					for (PrivilegeGroup privilegeGroup : applicationRole.getPrivilegeGroups()) {
						ApplicationPrivilegeGroup applicationPrivilegeGroup = groupMap.get(applicationName + "." + privilegeGroup.getName());

					}

				}
				OrganizationField organizationField = roleApplicationRoleAssignment.getOrganizationFieldFilter();
				OrganizationUnit organizationRoot = roleApplicationRoleAssignment.getFixedOrganizationRoot();
				List<OrganizationUnitType> organizationUnitTypeFilter = roleApplicationRoleAssignment.getOrganizationUnitTypeFilter();

				//calculatePrivilegesFromApplicationRoleAssignment(organizationUnit, roleApplicationRoleAssignment);
			}

			for (RolePrivilegeAssignment privilegeAssignment : privilegeRole.getPrivilegeAssignments()) {
//				privilegeAssignment.get
//				calculatePrivilegesFromRolePrivilegeAssignment(organizationUnit, privilegeAssignment);
			}
		}
		return null;
	}
}
