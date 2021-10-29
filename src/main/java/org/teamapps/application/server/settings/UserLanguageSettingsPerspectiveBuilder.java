package org.teamapps.application.server.settings;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;

public class UserLanguageSettingsPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public UserLanguageSettingsPerspectiveBuilder() {
		super("language", ApplicationIcons.DICTIONARY, Dictionary.LANGUAGE, "userSettings.language.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return true;
	}

	@Override
	public boolean autoProvisionPerspective() {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		return new UserLanguageSettingsPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
	}
}
