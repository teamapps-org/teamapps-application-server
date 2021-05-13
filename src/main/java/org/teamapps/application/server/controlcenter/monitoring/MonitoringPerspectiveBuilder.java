package org.teamapps.application.server.controlcenter.monitoring;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.controlcenter.Privileges;
import org.teamapps.databinding.MutableValue;

public class MonitoringPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public MonitoringPerspectiveBuilder() {
		super("monitoringPerspective", ApplicationIcons.CHART_LINE, "monitoring.title", "monitoring.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return applicationPrivilegeProvider.isAllowed(Privileges.LAUNCH_PERSPECTIVE_MONITORING);
	}

	@Override
	public boolean autoProvisionPerspective() {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new MonitoringPerspective(applicationInstanceData, mutableValue);
	}
}
