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

	public static SimplePrivilege LAUNCH_APPLICATION = PRIVILEGE_BUILDER.LAUNCH_APPLICATION;

	public static StandardPrivilegeGroup LAUNCH_APPLICATION2 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv", ApplicationIcons.OK, "privs.ok", "desc..", CREATE, READ, UPDATE, DELETE, PRINT);
	public static StandardPrivilegeGroup LAUNCH_APPLICATION3 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv2", ApplicationIcons.OK, "privs.ok2", "desc..2", CREATE, READ, UPDATE, DELETE, PRINT, EXECUTE);
	public static StandardPrivilegeGroup LAUNCH_APPLICATION4 = PRIVILEGE_BUILDER.addStandardPrivilegeGroup("testPriv3", ApplicationIcons.USERS_CROWD, "privs.ok3", "desc..2", CREATE, READ, UPDATE, DELETE, PRINT, EXECUTE, CUSTOM);


	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}

}
