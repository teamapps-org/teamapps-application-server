package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.AbstractApplicationPerspective;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.OrganizationField;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.databinding.MutableValue;

public abstract class AbstractManagedApplicationPerspective extends AbstractApplicationPerspective {

	private ManagedApplication managedApplication;
	private Application mainApplication;
	private OrganizationField organizationField;

	public AbstractManagedApplicationPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		init();
	}

	private void init() {
		managedApplication = ManagedApplication.getById(getApplicationInstanceData().getManagedApplicationId());
		mainApplication = managedApplication.getMainApplication();
		organizationField = OrganizationUtils.convert(getApplicationInstanceData().getOrganizationField());
	}

	public void handleOnAfterLoad() {

	}

	public boolean isAppFilter() {
		return isOrgFieldFilterApplied() && getMainApplication() != null;
	}

	public boolean isOrgFieldFilterApplied() {
		return organizationField != null;
	}

	public ManagedApplication getManagedApplication() {
		return managedApplication;
	}

	public Application getMainApplication() {
		return mainApplication;
	}

	public OrganizationField getOrganizationField() {
		return organizationField;
	}
}
