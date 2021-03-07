package org.teamapps.application.server.controlcenter.localization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.databinding.MutableValue;

public class LocalizationPerspective extends AbstractManagedApplicationPerspective {

	public LocalizationPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
	}

}
