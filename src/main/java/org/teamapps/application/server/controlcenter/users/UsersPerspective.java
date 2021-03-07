package org.teamapps.application.server.controlcenter.users;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.databinding.MutableValue;

public class UsersPerspective extends AbstractManagedApplicationPerspective {

	public UsersPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {

	}

}

