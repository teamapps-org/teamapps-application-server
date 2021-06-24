package org.teamapps.application.server.priv;

import org.teamapps.application.api.privilege.*;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.composite.CompositeIcon;

import java.util.List;

public class ChatPrivileges {

	private final static ApplicationPrivilegeBuilder PRIVILEGE_BUILDER = new ApplicationPrivilegeBuilder();
	private final static ApplicationRoleBuilder ROLE_BUILDER = new ApplicationRoleBuilder();

	public final static SimplePrivilege LAUNCH_APPLICATION = PRIVILEGE_BUILDER.LAUNCH_APPLICATION;
	public final static SimplePrivilege LAUNCH_PERSPECTIVE_CHAT = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveChat", ApplicationIcons.MESSAGES, "launch chat perspective", "launch chat perspective");


	public static List<ApplicationRole> getRoles() {
		return ROLE_BUILDER.getRoles();
	}

	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}
}
