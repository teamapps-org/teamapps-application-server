package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class OrganizationChartPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public OrganizationChartPerspectiveBuilder() {
		super("organizationChartPerspective", ApplicationIcons.PIECES, "organizationChart.title", "organizationChart.desc");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {
		return new OrganizationChartPerspective(applicationInstanceData, mutableValue);
	}
}
