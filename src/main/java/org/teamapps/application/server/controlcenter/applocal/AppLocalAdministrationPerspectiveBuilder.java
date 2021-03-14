package org.teamapps.application.server.controlcenter.applocal;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class AppLocalAdministrationPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public AppLocalAdministrationPerspectiveBuilder() {
		super("appLocalAdministration", ApplicationIcons.WINDOW_KEY, "appLocalAdministration.title", "appLocalAdministration.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new AppLocalAdministrationPerspective(applicationInstanceData, mutableValue);
	}
}
