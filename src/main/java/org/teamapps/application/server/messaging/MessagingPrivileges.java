package org.teamapps.application.server.messaging;

import org.teamapps.application.api.privilege.*;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.composite.CompositeIcon;

import java.util.List;

public class MessagingPrivileges {

	private final static ApplicationPrivilegeBuilder PRIVILEGE_BUILDER = new ApplicationPrivilegeBuilder();
	private final static ApplicationRoleBuilder ROLE_BUILDER = new ApplicationRoleBuilder();

	public final static SimplePrivilege LAUNCH_APPLICATION = PRIVILEGE_BUILDER.LAUNCH_APPLICATION;

	public final static SimplePrivilege LAUNCH_PERSPECTIVE_NEWS_BOARD = PRIVILEGE_BUILDER.addSimplePrivilege("launchPerspectiveNewsBoard", CompositeIcon.of(ApplicationIcons.KEY, ApplicationIcons.WORKER), "newsBoard.launchNewsBoardPerspective", "newsBoard.launchNewsBoardPerspective");
	public final static SimplePrivilege NEWS_BOARD_ADMIN_ACCESS = PRIVILEGE_BUILDER.addSimplePrivilege("newsBoardAdminAccess", ApplicationIcons.PILOT, "newsBoard.newsBoardAdmin", "newsBoard.newsBoardAdmin");


	public static List<ApplicationRole> getRoles() {
		return ROLE_BUILDER.getRoles();
	}

	public static List<PrivilegeGroup> getPrivileges() {
		return PRIVILEGE_BUILDER.getPrivileges();
	}
}