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

import org.teamapps.application.api.privilege.ApplicationPrivilegeBuilder;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.privilege.SimplePrivilege;
import org.teamapps.application.api.privilege.StandardPrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;

import java.util.List;

import static org.teamapps.application.api.privilege.Privilege.*;

public class Privileges {

	private final static ApplicationPrivilegeBuilder PRIVILEGE_BUILDER = new ApplicationPrivilegeBuilder();

	public final static SimplePrivilege LAUNCH_APPLICATION = PRIVILEGE_BUILDER.LAUNCH_APPLICATION;
	public final static StandardPrivilegeGroup LAUNCH_APPLICATION2 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv", ApplicationIcons.OK, "privs.ok", "desc..", CREATE, READ, UPDATE, DELETE, PRINT);
	public final static StandardPrivilegeGroup LAUNCH_APPLICATION3 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv2", ApplicationIcons.OK, "privs.ok2", "desc..2", CREATE, READ, UPDATE, DELETE, PRINT, EXECUTE);
	public final static StandardPrivilegeGroup LAUNCH_APPLICATION4 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv3", ApplicationIcons.USERS_CROWD, "privs.ok3", "desc..2", CREATE, READ, UPDATE, DELETE, PRINT, EXECUTE, CUSTOM);

	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}
}
