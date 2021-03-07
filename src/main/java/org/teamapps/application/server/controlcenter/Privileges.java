package org.teamapps.application.server.controlcenter;

import org.teamapps.application.api.privilege.AbstractApplicationPrivileges;
import org.teamapps.application.api.privilege.StandardPrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;

import static org.teamapps.application.api.privilege.Privilege.*;

public class Privileges extends AbstractApplicationPrivileges {

	public static StandardPrivilegeGroup LAUNCH_APPLICATION2 = addStandardPrivilegeGroup("testPriv", ApplicationIcons.OK, "privs.ok", "desc..", CREATE, READ, UPDATE, DELETE, PRINT);
	public static StandardPrivilegeGroup LAUNCH_APPLICATION3 = addStandardPrivilegeGroup("testPriv2", ApplicationIcons.OK, "privs.ok2", "desc..2", CREATE, READ, UPDATE, DELETE, PRINT, EXECUTE);
}
