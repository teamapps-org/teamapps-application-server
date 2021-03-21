package org.teamapps.application.server.controlcenter.users;

import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.model.controlcenter.User;

public class UserUtils {

	public static String getProfilePictureLink(User user, UserSessionData userSessionData) {
		return userSessionData.getRegistry().getBaseResourceLinkProvider().getUserProfilePictureLink(user);
	}
}
