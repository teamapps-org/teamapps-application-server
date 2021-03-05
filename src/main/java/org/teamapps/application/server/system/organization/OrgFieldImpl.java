package org.teamapps.application.server.system.organization;

import org.teamapps.application.api.organization.OrgField;
import org.teamapps.model.controlcenter.OrganizationField;

public class OrgFieldImpl implements OrgField {

	private final OrganizationField organizationField;

	public OrgFieldImpl(OrganizationField organizationField) {
		this.organizationField = organizationField;
	}

	@Override
	public int getId() {
		return organizationField.getId();
	}

	@Override
	public String getTitle() {
		return organizationField.getTitle().getOriginalLanguage(); //todo!
	}
}
