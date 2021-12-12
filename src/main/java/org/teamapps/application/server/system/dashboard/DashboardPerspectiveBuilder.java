package org.teamapps.application.server.system.dashboard;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;

public class DashboardPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public DashboardPerspectiveBuilder() {
		super("dashboard", ApplicationIcons.CHART_SPLINE, "Dashboard", "Dashboard");
	}

	@Override
	public boolean autoProvisionPerspective() {
		return true;
	}

	@Override
	public boolean useToolbarPerspectiveMenu() {
		return super.useToolbarPerspectiveMenu();
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		return new DashboardPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
	}
}
