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
package org.teamapps.application.server.controlcenter.roles;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.privilege.Privilege;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.controlcenter.Privileges;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.application.tools.PrivilegeUtils;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.application.ux.form.FormController;
import org.teamapps.application.ux.view.MasterDetailController;
import org.teamapps.databinding.MutableValue;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.pojo.Query;
import org.teamapps.ux.component.field.AbstractField;
import org.teamapps.ux.component.field.CheckBox;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UserRoleAssignmentPerspective extends AbstractManagedApplicationPerspective {

	private final UserSessionData userSessionData;


	public UserRoleAssignmentPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		Supplier<Query<UserRoleAssignment>> querySupplier = () -> isAppFilter() ? UserRoleAssignment.filter().filterRole(Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId()))) : UserRoleAssignment.filter();
		MasterDetailController<UserRoleAssignment> masterDetailController = new MasterDetailController<>(ApplicationIcons.USERS_THREE_RELATION, getLocalized("userRoleAssignment.userRoleAssignments"), getApplicationInstanceData(), querySupplier, Privileges.USER_ROLE_ASSIGNMENT_PERSPECTIVE, UserRoleAssignment.FIELD_ORGANIZATION_UNIT);
		EntityModelBuilder<UserRoleAssignment> entityModelBuilder = masterDetailController.getEntityModelBuilder();
		FormController<UserRoleAssignment> formController = masterDetailController.getFormController();
		ResponsiveForm<UserRoleAssignment> form = masterDetailController.getResponsiveForm();

		Table<UserRoleAssignment> table = entityModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(28);
		table.setStripedRows(false);
		entityModelBuilder.updateModels();

		TemplateField<User> userTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider(getApplicationInstanceData()));
		TemplateField<Role> roleTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()));
		TemplateField<OrganizationUnit> orgUnitTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitPropertyProvider(getApplicationInstanceData()));

		table.addColumn(new TableColumn<UserRoleAssignment, User>(UserRoleAssignment.FIELD_USER, getLocalized("userRoleAssignment.user"), userTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<UserRoleAssignment, Role>(UserRoleAssignment.FIELD_ROLE, getLocalized("userRoleAssignment.role"), roleTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<UserRoleAssignment, OrganizationUnit>(UserRoleAssignment.FIELD_ORGANIZATION_UNIT, getLocalized("userRoleAssignment.orgUnit"), orgUnitTableField).setDefaultWidth(200));

		entityModelBuilder.setCustomFieldSorter(fieldName -> {
			Comparator<String> comparator = getUser().getComparator(true);
			List<String> rankedLanguages = getUser().getRankedLanguages();
			return switch (fieldName) {
				case UserRoleAssignment.FIELD_USER -> (r1, r2) -> comparator.compare(r1.getUser().getLastName(), r2.getUser().getLastName());
				case UserRoleAssignment.FIELD_ROLE -> (r1, r2) -> comparator.compare(r1.getRole().getTitle().getText(rankedLanguages), r2.getRole().getTitle().getText(rankedLanguages));
				case UserRoleAssignment.FIELD_ORGANIZATION_UNIT -> (r1, r2) -> comparator.compare(r1.getOrganizationUnit().getName().getText(rankedLanguages), r2.getOrganizationUnit().getName().getText(rankedLanguages));
				default -> null;
			};
		});

		entityModelBuilder.setCustomFullTextFilter((r, query) -> {
			List<String> rankedLanguages = getUser().getRankedLanguages();
			return r.getUser() != null && r.getOrganizationUnit() != null && r.getRole() != null && (matches(r.getUser().getFirstName(), query) ||
					matches(r.getUser().getLastName(), query) ||
					matches(r.getRole().getTitle().getText(rankedLanguages), query) ||
					matches(r.getOrganizationUnit().getName().getText(rankedLanguages), query));
		});

		table.setPropertyExtractor((userRoleAssignment, propertyName) -> switch (propertyName) {
			case UserRoleAssignment.FIELD_USER -> userRoleAssignment.getUser();
			case UserRoleAssignment.FIELD_ROLE -> userRoleAssignment.getRole();
			case UserRoleAssignment.FIELD_ORGANIZATION_UNIT -> userRoleAssignment.getOrganizationUnit();
			default -> null;
		});

		ComboBox<User> userCombobox = ComboBoxUtils.createComboBox(query -> query == null || query.isBlank() ?
						User.getAll().stream().limit(50).collect(Collectors.toList()) :
						User.filter().parseFullTextFilter(query).execute().stream().limit(50).collect(Collectors.toList()),
				PropertyProviders.createUserPropertyProvider(getApplicationInstanceData()), BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		ComboBox<Role> roleComboBox = ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.getAll(),
				PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES
		);

		AbstractField<OrganizationUnitView> organizationComboBox = formController.getOrganizationUnitViewField(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, false);
		CheckBox mainResponsibleField = new CheckBox(getLocalized("userRoleAssignment.mainResponsible"));


		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.user"), userCombobox);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.role"), roleComboBox);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.orgUnit"), organizationComboBox);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.mainResponsible"), mainResponsibleField);

		masterDetailController.createViews(getPerspective(), table, formLayout);

		formController.addNotNull(userCombobox);
		formController.addNotNull(roleComboBox);
		formController.addNotNull(organizationComboBox);
		formController.addValidator(roleComboBox, role -> {
			OrganizationUnit organizationUnit = OrganizationUtils.convert(organizationComboBox.getValue());
			if (organizationUnit != null && role != null) {
				OrganizationUnitType type = organizationUnit.getType();
				List<OrganizationUnitType> unitTypes = role.getAllowedOrganizationUnitTypes();
				if (!unitTypes.isEmpty() && !unitTypes.contains(type)) {
					return getLocalized("userRoleAssignment.wrongOrgUnitForThisRole");
				}
			}
			return null;
		});
		formController.setSaveEntityHandler(userRoleAssignment -> {
			userRoleAssignment
					.setUser(userCombobox.getValue())
					.setRole(roleComboBox.getValue())
					.setOrganizationUnit(OrganizationUtils.convert(organizationComboBox.getValue()))
					.setMainResponsible(mainResponsibleField.getValue())
					.setLastVerified(Instant.now());
			return true;
		});

		entityModelBuilder.getOnSelectionEvent().addListener(userRoleAssignment -> {
			userCombobox.setValue(userRoleAssignment.getUser());
			roleComboBox.setValue(userRoleAssignment.getRole());
			organizationComboBox.setValue(OrganizationUtils.convert(userRoleAssignment.getOrganizationUnit()));
			mainResponsibleField.setValue(userRoleAssignment.isMainResponsible());
		});
		entityModelBuilder.setSelectedRecord(UserRoleAssignment.create());
	}

	private boolean matches(String value, String query) {
		return value != null && value.toLowerCase().contains(query);
	}

}

