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
package org.teamapps.application.server.controlcenter;

import org.teamapps.application.api.privilege.*;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.composite.CompositeIcon;

import java.util.List;

public class Privileges {

	private final static ApplicationPrivilegeBuilder PRIVILEGE_BUILDER = new ApplicationPrivilegeBuilder();

	private final static ApplicationRoleBuilder ROLE_BUILDER = new ApplicationRoleBuilder();

	public final static SimplePrivilege LAUNCH_APPLICATION = PRIVILEGE_BUILDER.LAUNCH_APPLICATION;
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ACCESS_CONTROL_APP_ROLE = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveAccessControlAppRole", CompositeIcon.of(ApplicationIcons.KEY, ApplicationIcons.WORKER), "accessControlAppRole.launch", "accessControlAppRole.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ACCESS_CONTROL = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveAccessControl", ApplicationIcons.KEYS, "accessControl.launch", "accessControl.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APPLICATION_GROUPS = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveApplicationGroups", ApplicationIcons.WINDOWS, "applicationGroups.launch", "applicationGroups.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APPLICATION_PROVISIONING = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveApplicationProvisioning", ApplicationIcons.INSTALL, "applicationProvisioning.launch", "applicationProvisioning.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APPLICATIONS = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveApplications", ApplicationIcons.BOX_SOFTWARE, "applications.launch", "applications.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APPLICATION_UPDATES = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveApplicationUpdates", ApplicationIcons.BOX_SOFTWARE, "applicationUpdates.launch", "applicationUpdates.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APP_LOCAL_ADMINISTRATION = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveAppLocalAdministration", ApplicationIcons.WINDOW_KEY, "appLocalAdministration.launch", "appLocalAdministration.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_DATABASE = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveDatabase", ApplicationIcons.DATA_TABLE, "database.launch", "database.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ORGANIZATION_CHART = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveOrganizationChart", ApplicationIcons.PIECES, "organizationChart.launch", "organizationChart.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ORGANIZATION_FIELD = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveOrganizationField", ApplicationIcons.ELEMENTS_TREE, "organizationField.launch", "organizationField.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ORGANIZATION = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveOrganization", ApplicationIcons.ELEMENTS_HIERARCHY, "organization.launch", "organization.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ORGANIZATION_UNIT_TYPE = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveOrganizationUnitType", ApplicationIcons.ELEMENTS_CASCADE, "organizationUnitType.launch", "organizationUnitType.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_ROLES = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveRoles", ApplicationIcons.WORKER, "roles.launch", "roles.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_USER_ROLE_ASSIGNMENT = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveUserRoleAssignment", ApplicationIcons.USERS_THREE_RELATION, "userRoleAssignment.launch", "userRoleAssignment.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_SYSTEM_LOG = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveSystemLog", ApplicationIcons.CONSOLE, "systemLog.launch", "systemLog.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_APPLICATION_CONFIGURATION = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveApplicationConfiguration", ApplicationIcons.CODE_LINE, "applicationConfiguration.launch", "applicationConfiguration.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_MACHINE_TRANSLATION = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveMachineTranslation", ApplicationIcons.EARTH_LINK, "machineTranslation.launch", "machineTranslation.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_TRANSLATIONS = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveTranslations", ApplicationIcons.SPELL_CHECK, "translations.launch", "translations.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_USERS = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveUsers", ApplicationIcons.USERS_CROWD, "users.launch", "users.desc");
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_MONITORING = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveMonitoring", ApplicationIcons.CHART_LINE, "monitoring.launch", "monitoring.desc");




	public final static OrganizationalPrivilegeGroup USERS_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultOrganizationalPrivilegeGroup("usersPerspective", ApplicationIcons.USERS_CROWD, "users.title", "users.desc");
	public final static OrganizationalPrivilegeGroup USER_ROLE_ASSIGNMENT_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultOrganizationalPrivilegeGroup("userRoleAssignmentPerspective", ApplicationIcons.USERS_THREE_RELATION, "userRoleAssignment.title", "userRoleAssignment.desc");
	public final static OrganizationalPrivilegeGroup ORGANIZATION_UNIT_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultOrganizationalPrivilegeGroup("organizationUnitPerspective", ApplicationIcons.ELEMENTS_HIERARCHY, "organization.title", "organization.desc");
	public final static StandardPrivilegeGroup ORGANIZATION_UNIT_TYPE_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("organizationUnitTypePerspective", ApplicationIcons.ELEMENTS_CASCADE, "organizationUnitType.title", "organizationUnitType.desc");
	public final static StandardPrivilegeGroup ORGANIZATION_FIELD_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("organizationFieldPerspective", ApplicationIcons.ELEMENTS_TREE, "organizationField.title", "organizationField.desc");
	public final static StandardPrivilegeGroup ROLES_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("rolesPerspective", ApplicationIcons.WORKER, "roles.title", "roles.desc");
	public final static StandardPrivilegeGroup ACCESS_CONTROL_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("accessControlPerspective", ApplicationIcons.KEYS, "accessControl.title", "accessControl.desc");
	public final static StandardPrivilegeGroup ACCESS_CONTROL_APP_ROLE_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("accessControlAppRolePerspective", CompositeIcon.of(ApplicationIcons.KEY, ApplicationIcons.WORKER), "accessControlAppRole.title", "accessControlAppRole.desc");
	//public final static StandardPrivilegeGroup APPLICATIONS_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("applications", ApplicationIcons.BOX_SOFTWARE, "applications.title", "applications.desc");
	public final static StandardPrivilegeGroup APPLICATION_PROVISIONING_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("applicationProvisioning", ApplicationIcons.INSTALL, "applicationProvisioning.title", "applicationProvisioning.desc");
	//public final static StandardPrivilegeGroup APPLICATION_GROUPS_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("applicationGroups", ApplicationIcons.WINDOWS, "applicationGroups.title", "applicationGroups.desc");
	//public final static StandardPrivilegeGroup APPLICATION_GROUPS_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("applicationGroups", ApplicationIcons.WINDOWS, "applicationGroups.title", "applicationGroups.desc");
	//public final static StandardPrivilegeGroup APPLICATION_CONFIGURATION_PERSPECTIVE = PRIVILEGE_BUILDER.addDefaultStandardPrivilegeGroup("applicationConfigurationPerspective", ApplicationIcons.CODE_LINE, "applicationConfiguration.title", "applicationConfiguration.desc");
	public final static StandardPrivilegeGroup SYSTEM_LOG_PERSPECTIVE = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("systemLogPerspective", ApplicationIcons.CONSOLE, "systemLog.title", "systemLog.desc", Privilege.READ);



	public final static ApplicationRole APPLICATION_ADMINISTRATOR_ROLE = ROLE_BUILDER.addRole("applicationAdministratorRole", ApplicationIcons.PILOT, "application.roles.applicationAdministrator", "application.roles.applicationAdministrator.desc",
			LAUNCH_PERSPECTIVE_APP_LOCAL_ADMINISTRATION,
			LAUNCH_PERSPECTIVE_TRANSLATIONS,
			LAUNCH_PERSPECTIVE_USER_ROLE_ASSIGNMENT,
			LAUNCH_PERSPECTIVE_ROLES,
			LAUNCH_PERSPECTIVE_ACCESS_CONTROL,
			LAUNCH_PERSPECTIVE_ACCESS_CONTROL_APP_ROLE,
			LAUNCH_PERSPECTIVE_APPLICATION_UPDATES,
			LAUNCH_PERSPECTIVE_SYSTEM_LOG,
			LAUNCH_PERSPECTIVE_APPLICATION_CONFIGURATION
	);

	public final static ApplicationRole APPLICATION_DEVELOPER_ROLE = ROLE_BUILDER.addRole("applicationDeveloperRole", ApplicationIcons.TEXT_BINARY, "application.roles.applicationDeveloper", "application.roles.applicationDeveloper.desc",
			LAUNCH_PERSPECTIVE_APP_LOCAL_ADMINISTRATION,
			LAUNCH_PERSPECTIVE_TRANSLATIONS,
			LAUNCH_PERSPECTIVE_USER_ROLE_ASSIGNMENT,
			LAUNCH_PERSPECTIVE_ROLES,
			LAUNCH_PERSPECTIVE_ACCESS_CONTROL,
			LAUNCH_PERSPECTIVE_ACCESS_CONTROL_APP_ROLE,
			LAUNCH_PERSPECTIVE_APPLICATION_UPDATES,
			LAUNCH_PERSPECTIVE_SYSTEM_LOG,
			LAUNCH_PERSPECTIVE_APPLICATION_CONFIGURATION
	);

	public static List<ApplicationRole> getRoles() {
		return ROLE_BUILDER.getRoles();
	}

	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}
}
