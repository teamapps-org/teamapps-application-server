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
package org.teamapps.application.server.controlcenter.accesscontrol;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.ui.FormMetaFields;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.model.ComboBoxModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccessControlAppRolePerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<RoleApplicationRoleAssignment> selectedRoleApplicationRoleAssignment = TwoWayBindableValue.create();
	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;


	public AccessControlAppRolePerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.KEYS, getLocalized("accessControl.accessControlListEntries"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.KEYS, getLocalized("accessControl.accessControlListEntry"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<RoleApplicationRoleAssignment> rolePrivilegeAssignmentModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? RoleApplicationRoleAssignment.filter().organizationFieldFilter(NumericFilter.equalsFilter(getOrganizationField().getId())) : RoleApplicationRoleAssignment.filter(), getApplicationInstanceData());
		rolePrivilegeAssignmentModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("accessControl.accessControlListEntries"));
		rolePrivilegeAssignmentModelBuilder.attachSearchField(masterView);
		rolePrivilegeAssignmentModelBuilder.onSelectedRecordChanged.addListener(selectedRoleApplicationRoleAssignment::set);
		Table<RoleApplicationRoleAssignment> table = rolePrivilegeAssignmentModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(28);
		table.setStripedRows(false);
		rolePrivilegeAssignmentModelBuilder.updateModels();


		TemplateField<Role> roleTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()));
		TemplateField<Application> applicationTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createApplicationPropertyProvider(userSessionData));
		TemplateField<RoleApplicationRoleAssignment> applicationRoleTemplateField = createApplicationRoleTemplateField();


		TemplateField<OrganizationUnit> customOrganizationUnitTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitPropertyProvider(getApplicationInstanceData()));

		table.addColumn(new TableColumn<RoleApplicationRoleAssignment>(RoleApplicationRoleAssignment.FIELD_ROLE, getLocalized("roles.role"), roleTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<RoleApplicationRoleAssignment>(RoleApplicationRoleAssignment.FIELD_APPLICATION, getLocalized("applications.application"), applicationTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<RoleApplicationRoleAssignment>(RoleApplicationRoleAssignment.FIELD_APPLICATION_ROLE_NAME, getLocalized("accessControl.applicationRole"), applicationRoleTemplateField).setDefaultWidth(200));
		table.addColumn(new TableColumn<RoleApplicationRoleAssignment>(RoleApplicationRoleAssignment.FIELD_FIXED_ORGANIZATION_ROOT, getLocalized("accessControl.customOrganizationUnit"), customOrganizationUnitTableField).setDefaultWidth(200));

		table.setPropertyExtractor((rolePrivilegeAssignment, propertyName) -> switch (propertyName) {
			case RoleApplicationRoleAssignment.FIELD_ROLE -> rolePrivilegeAssignment.getRole();
			case RoleApplicationRoleAssignment.FIELD_APPLICATION -> rolePrivilegeAssignment.getApplication();
			case RoleApplicationRoleAssignment.FIELD_APPLICATION_ROLE_NAME -> rolePrivilegeAssignment;
			case RoleApplicationRoleAssignment.FIELD_FIXED_ORGANIZATION_ROOT -> rolePrivilegeAssignment.getFixedOrganizationRoot();
			default -> null;
		});
		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		ComboBox<Role> roleComboBox = ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.getAll(),
				PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES
		);

		ComboBox<Application> applicationComboBox = createApplicationComboBox();
		ComboBox<ApplicationRole> applicationRoleComboBox = createApplicationRoleComboBox(applicationComboBox);
		ComboBox<OrganizationField> organizationFieldFilterComboBox = createOrganizationFieldComboBox();
		ComboBox<OrganizationUnit> organizationFilterComboBox = OrganizationUtils.createOrganizationComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, OrganizationUnit.getAll(), getApplicationInstanceData());
		TagComboBox<OrganizationUnitType> organizationUnitTypeFilterTagComboBox = OrganizationUtils.createOrganizationUnitTypeTagComboBox(50, getApplicationInstanceData());


		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("roles.role"), roleComboBox);
		formLayout.addLabelAndField(null, getLocalized("applications.application"), applicationComboBox);
		formLayout.addLabelAndField(null, getLocalized("accessControl.applicationRole"), applicationRoleComboBox);
		if (!isOrgFieldFilterApplied()) {
			formLayout.addLabelAndField(null, getLocalized("accessControl.organizationFieldFilter"), organizationFieldFilterComboBox);
		}
		formLayout.addLabelAndField(null, getLocalized("accessControl.customOrganizationUnit"), organizationFilterComboBox);
		formLayout.addLabelAndField(null, getLocalized("accessControl.organizationUnitTypeFilter"), organizationUnitTypeFilterTagComboBox);

		FormMetaFields formMetaFields = getApplicationInstanceData().getComponentFactory().createFormMetaFields();
		formMetaFields.addMetaFields(formLayout, false);
		selectedRoleApplicationRoleAssignment.onChanged().addListener(formMetaFields::updateEntity);

		detailView.setComponent(form);

		applicationComboBox.onValueChanged.addListener(() -> {
			applicationRoleComboBox.setValue(null);
		});


		addButton.onClick.addListener(() -> selectedRoleApplicationRoleAssignment.set(RoleApplicationRoleAssignment.create()));

		saveButton.onClick.addListener(() -> {
			RoleApplicationRoleAssignment roleApplicationRoleAssignment = selectedRoleApplicationRoleAssignment.get();
			OrganizationField organizationField = isAppFilter() ? getOrganizationField() : organizationFieldFilterComboBox.getValue();
			if (roleApplicationRoleAssignment != null && roleComboBox.getValue() != null && applicationComboBox.getValue() != null && applicationRoleComboBox.getValue() != null) {
				roleApplicationRoleAssignment
						.setRole(roleComboBox.getValue())
						.setApplication(applicationComboBox.getValue())
						.setApplicationRoleName(applicationRoleComboBox.getValue().getName())
						.setOrganizationFieldFilter(organizationField)
						.setFixedOrganizationRoot(organizationFilterComboBox.getValue())
						.setOrganizationUnitTypeFilter(organizationUnitTypeFilterTagComboBox.getValue())
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				rolePrivilegeAssignmentModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedRoleApplicationRoleAssignment.onChanged().addListener(roleApplicationRoleAssignment -> {
			roleComboBox.setValue(roleApplicationRoleAssignment.getRole());
			applicationComboBox.setValue(roleApplicationRoleAssignment.getApplication());
			applicationRoleComboBox.setValue(getApplicationRole(roleApplicationRoleAssignment));
			organizationFieldFilterComboBox.setValue(roleApplicationRoleAssignment.getOrganizationFieldFilter());
			organizationFilterComboBox.setValue(roleApplicationRoleAssignment.getFixedOrganizationRoot());
			organizationUnitTypeFilterTagComboBox.setValue(roleApplicationRoleAssignment.getOrganizationUnitTypeFilter());
		});

		selectedRoleApplicationRoleAssignment.set(RoleApplicationRoleAssignment.create());
	}

	private ComboBox<OrganizationField> createOrganizationFieldComboBox() {
		return ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Collections.singletonList(getOrganizationField()) : OrganizationField.getAll(),
				PropertyProviders.createOrganizationFieldPropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE
		);
	}

	private ComboBox<ApplicationRole> createApplicationRoleComboBox(ComboBox<Application> applicationComboBox) {
		ComboBox<ApplicationRole> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		Supplier<List<ApplicationRole>> recordsSupplier = () -> {
			LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(applicationComboBox.getValue());
			if (loadedApplication != null) {
				List<ApplicationRole> applicationRoles = loadedApplication.getBaseApplicationBuilder().getApplicationRoles();
				return applicationRoles != null ? applicationRoles : Collections.emptyList();
			} else {
				return Collections.emptyList();
			}
		};
		PropertyProvider<ApplicationRole> propertyProvider = (applicationRole, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(applicationComboBox.getValue());
			Map<String, Object> map = new HashMap<>();
			if (localizationProvider == null) {
				return map;
			}
			map.put(BaseTemplate.PROPERTY_ICON, applicationRole.getIcon());
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(applicationRole.getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(applicationRole.getDescriptionKey()));
			return map;

		};
		comboBox.setPropertyProvider(propertyProvider);
		Function<ApplicationRole, String> recordToStringFunction = UiUtils.createRecordToStringFunction(propertyProvider);
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(query -> query == null || query.isBlank() ? recordsSupplier.get() : recordsSupplier.get().stream().filter(record -> recordToStringFunction.apply(record).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));

		return comboBox;
	}

	private TemplateField<RoleApplicationRoleAssignment> createApplicationRoleTemplateField() {
		TemplateField<RoleApplicationRoleAssignment> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		PropertyProvider<RoleApplicationRoleAssignment> propertyProvider = (assignment, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(assignment.getApplication());
			Map<String, Object> map = new HashMap<>();
			if (localizationProvider == null || assignment.getApplicationRoleName() == null) {
				return map;
			}
			ApplicationRole applicationRole = getApplicationRole(assignment);
			if (applicationRole == null) {
				return map;
			}
			map.put(BaseTemplate.PROPERTY_ICON, applicationRole.getIcon());
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(applicationRole.getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(applicationRole.getDescriptionKey()));
			return map;
		};
		templateField.setPropertyProvider(propertyProvider);
		return templateField;
	}

	private ApplicationRole getApplicationRole(RoleApplicationRoleAssignment assignment) {
		if (assignment == null || assignment.getApplication() == null || assignment.getApplicationRoleName() == null) {
			return null;
		}
		LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(assignment.getApplication());
		if (loadedApplication != null) {
			return loadedApplication.getBaseApplicationBuilder().getApplicationRoles().stream().filter(appRole -> assignment.getApplicationRoleName().equals(appRole.getName())).findAny().orElse(null);
		} else {
			return null;
		}
	}

	private ComboBox<Application> createApplicationComboBox() {
		ComboBox<Application> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		PropertyProvider<Application> propertyProvider = PropertyProviders.createApplicationPropertyProvider(userSessionData);
		Function<Application, String> recordToString = UiUtils.createRecordToStringFunction(propertyProvider);
		ComboBoxModel<Application> model = ComboBoxUtils.createComboBoxModel(this::getAvailableApplications, propertyProvider, 50);
		comboBox.setModel(model);
		comboBox.setPropertyProvider(propertyProvider);
		comboBox.setRecordToStringFunction(recordToString);
		return comboBox;
	}

	private List<Application> getAvailableApplications() {
		if (isAppFilter()) {
			return getManagedApplication().getPerspectives().stream()
					.map(p -> p.getApplicationPerspective().getApplication())
					.distinct()
					.collect(Collectors.toList());
		} else {
			return Application.getAll();
		}
	}

}

