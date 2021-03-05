package org.teamapps.application.server.controlcenter.localization;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.controlcenter.apps.ApplicationUpdatesPerspective;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;

public class LocalizationPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public LocalizationPerspectiveBuilder() {
		super("localizationPerspective", ApplicationIcons.SPEECH_BALLOON, "applications.title", "applications.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new LocalizationPerspective(applicationInstanceData, mutableValue);
	}
}
