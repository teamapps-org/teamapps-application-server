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

import static org.teamapps.application.api.privilege.Privilege.*;

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


	public final static StandardPrivilegeGroup ABC = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("test", null, null, null, CREATE, READ, UPDATE, DELETE, RESTORE, SHOW_RECYCLE_BIN, EXECUTE, PRINT);


	public final static ApplicationRole role1 = ROLE_BUILDER.addRole("role", ApplicationIcons.PILOT, "title", "test",
			ABC.createCopyWithPrivileges(CREATE, READ),
			LAUNCH_PERSPECTIVE_ACCESS_CONTROL
	);

	public static List<ApplicationRole> getRoles() {
		return ROLE_BUILDER.getRoles();
	}

	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}
}
