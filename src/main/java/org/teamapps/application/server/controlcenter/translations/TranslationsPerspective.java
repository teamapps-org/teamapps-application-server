package org.teamapps.application.server.controlcenter.translations;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.databinding.MutableValue;

public class TranslationsPerspective extends AbstractManagedApplicationPerspective {

	public TranslationsPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
	}

}
