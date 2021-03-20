package org.teamapps.application.server.system.bootstrap;

import org.teamapps.application.server.system.server.SecureLinkBuilder;
import org.teamapps.application.server.system.server.SecureResourceHandler;
import org.teamapps.model.controlcenter.User;

public class BaseResourceLinkProvider {


	private final SecureResourceHandler secureResourceHandler;
	private final SecureLinkBuilder userStandardProfilePictureLinkBuilder;
	private final SecureLinkBuilder userLargeProfilePictureLinkBuilder;

	public BaseResourceLinkProvider() {
		secureResourceHandler = SecureResourceHandler.getInstance();
		userStandardProfilePictureLinkBuilder = secureResourceHandler.registerByteArrayResourceHandler(id -> User.getById(id).getProfilePicture(), "jpg");
		userLargeProfilePictureLinkBuilder = secureResourceHandler.registerByteArrayResourceHandler(id -> User.getById(id).getProfilePictureLarge(), "jpg");
	}


	public String getUserProfilePictureLink(User user) {
		return userStandardProfilePictureLinkBuilder.createLink(user.getId());
	}

	public String getUserLargeProfilePictureLink(User user) {
		return userLargeProfilePictureLinkBuilder.createLink(user.getId());
	}

}
