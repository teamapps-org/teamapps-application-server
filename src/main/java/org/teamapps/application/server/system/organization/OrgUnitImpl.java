package org.teamapps.application.server.system.organization;

import org.teamapps.application.api.organization.OrgUnit;
import org.teamapps.model.controlcenter.OrganizationUnit;

import java.util.List;
import java.util.stream.Collectors;

public class OrgUnitImpl implements OrgUnit {

	private final OrganizationUnit organizationUnit;

	public OrgUnitImpl(OrganizationUnit organizationUnit) {
		this.organizationUnit = organizationUnit;
	}

	@Override
	public OrgUnit getParent() {
		return organizationUnit.getParent() != null ? new OrgUnitImpl(organizationUnit.getParent()) : null;
	}

	@Override
	public List<OrgUnit> getChildren() {
		return organizationUnit.getChildren().stream()
				.map(OrgUnitImpl::new)
				.collect(Collectors.toList());
	}

	@Override
	public String getTitle() {
		return organizationUnit.getName().getOriginalLanguage();
	}

	@Override
	public int getId() {
		return organizationUnit.getId();
	}

	@Override
	public String getQualifiedName() {
		return organizationUnit.getQualifiedName();
	}
}
