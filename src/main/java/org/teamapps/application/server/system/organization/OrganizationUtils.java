package org.teamapps.application.server.system.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.organization.OrgField;
import org.teamapps.application.api.organization.OrgUnit;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.model.controlcenter.*;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.model.ListTreeModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrganizationUtils {

	public static Set<OrgUnit> convertSet(Collection<OrganizationUnit> organizationUnits) {
		return organizationUnits.stream()
				.map(OrgUnitImpl::new)
				.collect(Collectors.toSet());
	}

	public static List<OrgUnit> convertList(Collection<OrganizationUnit> organizationUnits) {
		return organizationUnits.stream()
				.map(OrgUnitImpl::new)
				.collect(Collectors.toList());
	}

	public static OrgUnit convert(OrganizationUnit organizationUnit) {
		return organizationUnit != null ? new OrgUnitImpl(organizationUnit) : null;
	}

	public static OrganizationUnit convert(OrgUnit orgUnit) {
		return orgUnit != null && orgUnit.getId() > 0 ? OrganizationUnit.getById(orgUnit.getId()) : null;
	}

	public static OrgField convert(OrganizationField organizationField) {
		return organizationField != null ? new OrgFieldImpl(organizationField) : null;
	}

	public static OrganizationField convert(OrgField orgField) {
		return orgField != null && orgField.getId() > 0 ? OrganizationField.getById(orgField.getId()) : null;
	}

	public static Set<OrganizationUnit> getAllUnits(OrganizationUnit unit, Collection<OrganizationUnitType> unitTypesFilter) {
		Set<OrganizationUnit> result = new HashSet<>();
		Set<OrganizationUnit> traversedNodes = new HashSet<>();
		Set<OrganizationUnitType> filter = unitTypesFilter != null ? new HashSet<>(unitTypesFilter) : null;
		calculateAllUnits(unit, filter, traversedNodes, result);
		return result;
	}

	private static void calculateAllUnits(OrganizationUnit unit, Set<OrganizationUnitType> unitTypesFilter, Set<OrganizationUnit> traversedNodes, Set<OrganizationUnit> result) {
		if (unitTypesFilter == null || unitTypesFilter.contains(unit.getType())) {
			result.add(unit);
		}
		for (OrganizationUnit child : unit.getChildren()) {
			if (!traversedNodes.contains(child)) {
				traversedNodes.add(child);
				calculateAllUnits(child, unitTypesFilter, traversedNodes, result);
			}
		}
	}

	public static Set<UserContainer> getAllUserContainers(OrganizationUnit unit, Collection<OrganizationUnitType> unitTypesFilter) {
		Set<OrganizationUnit> allUnits = getAllUnits(unit, unitTypesFilter);
		Set<UserContainer> result = new HashSet<>();
		for (OrganizationUnit organizationUnit : allUnits) {
			UserContainer userContainer = organizationUnit.getUserContainer();
			if (userContainer != null) {
				result.add(userContainer);
			}
		}
		return result;
	}

	public static Set<User> getAllUsers(OrganizationUnit unit, Collection<OrganizationUnitType> unitTypesFilter) {
		Set<UserContainer> allUserContainers = getAllUserContainers(unit, unitTypesFilter);
		Set<User> result = new HashSet<>();
		for (UserContainer userContainer : allUserContainers) {
			List<User> users = userContainer.getUsers();
			result.addAll(users);
		}
		return result;
	}

	public static ComboBox<OrganizationField> createOrganizationFieldCombo(ApplicationInstanceData applicationInstanceData) {
		ComboBox<OrganizationField> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setModel(new ListTreeModel<>(OrganizationField.getAll()));
		comboBox.setPropertyProvider(PropertyProviders.createOrganizationFieldPropertyProvider(applicationInstanceData));
		return comboBox;
	}

	public static int getLevel(OrganizationUnit unit) {
		int level = 0;
		OrganizationUnit parent = unit.getParent();
		while (parent != null) {
			level++;
			parent = parent.getParent();
		}
		return level;
	}

	public static OrganizationUnit getParentWithGeoType(OrganizationUnit unit, GeoLocationType type) {
		if (unit == null) {
			return null;
		}
		OrganizationUnit parent = unit.getParent();
		while (parent != null) {
			if (parent.getType().getGeoLocationType() == type) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}
}
