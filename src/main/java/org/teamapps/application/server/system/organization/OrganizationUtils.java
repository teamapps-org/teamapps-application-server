/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.system.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;

import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.model.controlcenter.*;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.component.tree.TreeNodeInfo;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.model.ComboBoxModel;
import org.teamapps.ux.model.ListTreeModel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrganizationUtils {

	public static Set<OrganizationUnitView> convertSet(Collection<OrganizationUnit> organizationUnits) {
		return organizationUnits.stream()
				.map(unit -> OrganizationUnitView.getById(unit.getId()))
				.collect(Collectors.toSet());
	}

	public static List<OrganizationUnitView> convertList(Collection<OrganizationUnit> organizationUnits) {
		return organizationUnits.stream()
				.map(unit -> OrganizationUnitView.getById(unit.getId()))
				.collect(Collectors.toList());
	}

	public static OrganizationUnitView convert(OrganizationUnit organizationUnit) {
		return organizationUnit != null ? OrganizationUnitView.getById(organizationUnit.getId()) : null;
	}

	public static OrganizationUnit convert(OrganizationUnitView OrganizationUnitView) {
		return OrganizationUnitView != null && OrganizationUnitView.getId() > 0 ? OrganizationUnit.getById(OrganizationUnitView.getId()) : null;
	}

	public static OrganizationFieldView convert(OrganizationField organizationField) {
		return organizationField != null ? OrganizationFieldView.getById(organizationField.getId()) : null;
	}

	public static OrganizationField convert(OrganizationFieldView OrganizationFieldView) {
		return OrganizationFieldView != null && OrganizationFieldView.getId() > 0 ? OrganizationField.getById(OrganizationFieldView.getId()) : null;
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

	public static ComboBox<UserContainer> createUserContainerComboBox(Template template, Collection<OrganizationUnit> allowedUnits, ApplicationInstanceData applicationInstanceData) {
		ComboBox<UserContainer> comboBox = new ComboBox<>(template);
		ComboBoxModel<UserContainer> model = new ComboBoxModel<>() {
			@Override
			public List<UserContainer> getRecords(String query) {
				return queryUserContainer(query, allowedUnits);
			}

			@Override
			public TreeNodeInfo getTreeNodeInfo(UserContainer unit) {
				return new TreeNodeInfoImpl<>(unit.getOrganizationUnit().getParent() != null ? unit.getOrganizationUnit().getParent().getUserContainer() : null);
			}
		};
		comboBox.setModel(model);
		comboBox.setShowExpanders(true);
		PropertyProvider<UserContainer> propertyProvider = PropertyProviders.creatUserContainerPropertyProvider(applicationInstanceData);
		comboBox.setPropertyProvider(propertyProvider);
		Function<UserContainer, String> recordToStringFunction = unit -> {
			Map<String, Object> values = propertyProvider.getValues(unit, Collections.singleton(BaseTemplate.PROPERTY_CAPTION));
			Object result = values.get(BaseTemplate.PROPERTY_CAPTION);
			return (String) result;
		};
		comboBox.setRecordToStringFunction(recordToStringFunction);
		return comboBox;
	}

	public static ComboBox<OrganizationUnit> createOrganizationComboBox(Template template, Collection<OrganizationUnit> allowedUnits, ApplicationInstanceData applicationInstanceData) {
		ComboBox<OrganizationUnit> comboBox = new ComboBox<>(template);
		ComboBoxModel<OrganizationUnit> model = new ComboBoxModel<>() {
			@Override
			public List<OrganizationUnit> getRecords(String query) {
				return queryOrganizationUnits(query, allowedUnits);
			}

			@Override
			public TreeNodeInfo getTreeNodeInfo(OrganizationUnit unit) {
				return new TreeNodeInfoImpl<>(unit.getParent());
			}
		};
		comboBox.setModel(model);
		comboBox.setShowExpanders(true);
		PropertyProvider<OrganizationUnit> propertyProvider = PropertyProviders.creatOrganizationUnitPropertyProvider(applicationInstanceData);
		comboBox.setPropertyProvider(propertyProvider);
		Function<OrganizationUnit, String> recordToStringFunction = unit -> {
			Map<String, Object> values = propertyProvider.getValues(unit, Collections.singleton(BaseTemplate.PROPERTY_CAPTION));
			Object result = values.get(BaseTemplate.PROPERTY_CAPTION);
			return (String) result;
		};
		comboBox.setRecordToStringFunction(recordToStringFunction);
		return comboBox;
	}

	public static List<OrganizationUnit> queryOrganizationUnits(String query, Collection<OrganizationUnit> allowedUnits) {
		return query == null || query.isBlank() ?
				allowedUnits.stream().limit(50).collect(Collectors.toList()) :
				OrganizationUnit.filter()
						.parseFullTextFilter(query)
						.execute()
						.stream()
						.filter(allowedUnits::contains)
						.limit(50)
						.collect(Collectors.toList());
	}

	public static List<UserContainer> queryUserContainer(String query, Collection<OrganizationUnit> allowedUnits) {
		return query == null || query.isBlank() ?
				allowedUnits.stream()
						.filter(unit -> unit.getUserContainer() != null)
						.map(OrganizationUnit::getUserContainer)
						.limit(50)
						.collect(Collectors.toList()) :
				OrganizationUnit.filter()
						.parseFullTextFilter(query)
						.execute()
						.stream()
						.filter(allowedUnits::contains)
						.filter(unit -> unit.getUserContainer() != null)
						.limit(50)
						.map(OrganizationUnit::getUserContainer)
						.collect(Collectors.toList());
	}

	public static TagComboBox<OrganizationUnitType> createOrganizationUnitTypeTagComboBox(int limit, ApplicationInstanceData applicationInstanceData) {
		TagComboBox<OrganizationUnitType> tagComboBox = new TagComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		tagComboBox.setModel(query -> query == null || query.isBlank() ?
				OrganizationUnitType.getAll().stream().limit(limit).collect(Collectors.toList()) :
				OrganizationUnitType.filter().parseFullTextFilter(query).execute().stream().limit(limit).collect(Collectors.toList())
		);
		PropertyProvider<OrganizationUnitType> propertyProvider = PropertyProviders.creatOrganizationUnitTypePropertyProvider(applicationInstanceData);
		tagComboBox.setPropertyProvider(propertyProvider);
		tagComboBox.setRecordToStringFunction(unitType -> (String) propertyProvider.getValues(unitType, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION));
		tagComboBox.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		tagComboBox.setDistinct(true);
		return tagComboBox;
	}
}
