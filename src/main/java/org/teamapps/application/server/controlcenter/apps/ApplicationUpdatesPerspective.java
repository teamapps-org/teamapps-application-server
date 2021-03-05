package org.teamapps.application.server.controlcenter.apps;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.databinding.MutableValue;

public class ApplicationUpdatesPerspective extends AbstractManagedApplicationPerspective {

	public ApplicationUpdatesPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
	}

	@Override
	public void createPerspectiveUI() {

	}
}
