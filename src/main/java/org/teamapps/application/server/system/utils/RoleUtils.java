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

import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.model.controlcenter.Role;
import org.teamapps.model.controlcenter.UserRoleAssignment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
		Set<Role> roleSet = new HashSet<>();
		if (withSpecializationRoles) {
			roleSet = getAllRoleInstances(role);
		} else {
			roleSet.add(role);
		}
		Set<OrganizationUnit> allUnits = OrganizationUtils.getAllUnits(organizationUnit, organizationUnitTypesFilter);
		return getUserRoleAssignments(roleSet, allUnits);
	}

	public static List<UserRoleAssignment> getUserRoleAssignments(Set<Role> roleSet, Set<OrganizationUnit> organizationUnits) {
		return UserRoleAssignment.getAll().stream()
				.filter(assignment -> roleSet.contains(assignment.getRole()))
				.filter(assignment -> organizationUnits.contains(assignment.getOrganizationUnit()))
				.collect(Collectors.toList());
	}
}
