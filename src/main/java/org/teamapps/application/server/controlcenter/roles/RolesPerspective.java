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
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.application.ux.localize.TranslatableField;
import org.teamapps.application.ux.localize.TranslatableTextUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.OrganizationField;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.model.controlcenter.Role;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.CheckBox;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

import java.util.Collections;
import java.util.stream.Collectors;

public class RolesPerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<Role> selectedRole = TwoWayBindableValue.create();


	public RolesPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.WORKER, getLocalized("roles.roles"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.WORKER, getLocalized("roles.role"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<Role> roleModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())) : Role.filter(), getApplicationInstanceData());
		roleModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("roles.roles"));
		roleModelBuilder.attachSearchField(masterView);
		roleModelBuilder.onSelectedRecordChanged.addListener(selectedRole::set);
		Table<Role> table = roleModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(28);
		table.setStripedRows(false);
		roleModelBuilder.updateModels();

		TemplateField<Role> roleTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()));
		TemplateField<OrganizationField> organizationFieldTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createOrganizationFieldPropertyProvider(getApplicationInstanceData()));
		TagComboBox<OrganizationUnitType> allowedOrganizationUnitTypesTableField = UiUtils.createTagComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData()));
		CheckBox noDirectMembershipsTableField = new CheckBox(getLocalized("roles.noDirectMemberships"));


		table.addColumn(new TableColumn<Role>(Role.FIELD_TITLE, getLocalized(Dictionary.NAME), roleTableField).setDefaultWidth(200));
		if (!isAppFilter()) {
			table.addColumn(new TableColumn<Role>(Role.FIELD_ORGANIZATION_FIELD, getLocalized("organizationField.organizationField"), organizationFieldTableField).setDefaultWidth(130));
		}
		table.addColumn(new TableColumn<Role>(Role.FIELD_ALLOWED_ORGANIZATION_UNIT_TYPES, getLocalized("roles.allowedOrganizationUnitTypes"), allowedOrganizationUnitTypesTableField).setDefaultWidth(350));
		table.addColumn(new TableColumn<Role>(Role.FIELD_NO_DIRECT_MEMBERSHIPS, getLocalized("roles.noMemberships"), noDirectMembershipsTableField).setDefaultWidth(200));

		table.setPropertyExtractor((role, propertyName) -> switch (propertyName) {
			case Role.FIELD_TITLE -> role;
			case Role.FIELD_ORGANIZATION_FIELD -> role.getOrganizationField();
			case Role.FIELD_ALLOWED_ORGANIZATION_UNIT_TYPES -> role.getAllowedOrganizationUnitTypes();
			case Role.FIELD_NO_DIRECT_MEMBERSHIPS -> role.getNoDirectMemberships();
			default -> null;
		});

		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		TranslatableField titleField = TranslatableTextUtils.createTranslatableField(getApplicationInstanceData());
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox();
		ComboBox<Role> parentRoleComboBox = ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.getAll(),
				PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE
		);
		TagComboBox<OrganizationUnitType> allowedOrganizationUnitTypesTagCombo = createOrgUnitTypeTagComboBox();
		ComboBox<OrganizationField> organizationFieldComboBox = ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Collections.singletonList(getOrganizationField()) : OrganizationField.getAll(),
				PropertyProviders.createOrganizationFieldPropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE
		);
		TagComboBox<Role> generalizationRolesTagCombo = createRoleTagComboBox();
		TagComboBox<Role> specializationRolesTagCombo = createRoleTagComboBox();
		TagComboBox<Role> privilegesReceivingRolesTagCombo = createRoleTagComboBox();
		TagComboBox<Role> privilegesSendingRolesTagCombo = createRoleTagComboBox();
		CheckBox noDirectMembershipsCheckBox = new CheckBox(getLocalized("roles.noDirectMemberships"));

		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("roles.role"), titleField);
		formLayout.addLabelAndField(null, getLocalized("roles.icon"), iconComboBox);
		formLayout.addLabelAndField(null, getLocalized("roles.parentRole"), parentRoleComboBox);
		formLayout.addLabelAndField(null, getLocalized("roles.allowedOrganizationUnitTypes"), allowedOrganizationUnitTypesTagCombo);
		if (!isOrgFieldFilterApplied()) {
			formLayout.addLabelAndField(null, getLocalized("roles.organizationField"), organizationFieldComboBox);
		}
		formLayout.addLabelAndField(null, getLocalized("roles.generalizationRoles"), generalizationRolesTagCombo);
		formLayout.addLabelAndField(null, getLocalized("roles.specializationRoles"), specializationRolesTagCombo);
		formLayout.addLabelAndField(null, getLocalized("roles.privilegesReceivingRoles"), privilegesReceivingRolesTagCombo);
		formLayout.addLabelAndField(null, getLocalized("roles.privilegesSendingRoles"), privilegesSendingRolesTagCombo);
		formLayout.addLabelAndField(null, getLocalized("roles.noMemberships"), noDirectMembershipsCheckBox);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> selectedRole.set(Role.create()));

		saveButton.onClick.addListener(() -> {
			Role role = selectedRole.get();
			if (role != null && titleField.getValue() != null && iconComboBox.getValue() != null) {
				OrganizationField organizationField = isOrgFieldFilterApplied() ? getOrganizationField() : organizationFieldComboBox.getValue();
				role
						.setTitle(titleField.getValue())
						.setIcon(IconUtils.encodeNoStyle(iconComboBox.getValue()))
						.setParent(parentRoleComboBox.getValue())
						.setAllowedOrganizationUnitTypes(allowedOrganizationUnitTypesTagCombo.getValue())
						.setOrganizationField(organizationField)
						.setGeneralizationRoles(generalizationRolesTagCombo.getValue())
						.setSpecializationRoles(specializationRolesTagCombo.getValue())
						.setPrivilegesReceivingRoles(privilegesReceivingRolesTagCombo.getValue())
						.setPrivilegesSendingRoles(privilegesSendingRolesTagCombo.getValue())
						.setNoDirectMemberships(noDirectMembershipsCheckBox.getValue())
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				roleModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedRole.onChanged().addListener(role -> {
			titleField.setValue(role.getTitle());
			iconComboBox.setValue(IconUtils.decodeIcon(role.getIcon()));
			parentRoleComboBox.setValue(role.getParent());
			allowedOrganizationUnitTypesTagCombo.setValue(role.getAllowedOrganizationUnitTypes());
			organizationFieldComboBox.setValue(role.getOrganizationField());
			generalizationRolesTagCombo.setValue(role.getGeneralizationRoles());
			specializationRolesTagCombo.setValue(role.getSpecializationRoles());
			privilegesReceivingRolesTagCombo.setValue(role.getPrivilegesReceivingRoles());
			privilegesSendingRolesTagCombo.setValue(role.getPrivilegesSendingRoles());
			noDirectMembershipsCheckBox.setValue(role.getNoDirectMemberships());
		});
		selectedRole.set(Role.create());
	}

	private TagComboBox<Role> createRoleTagComboBox() {
		TagComboBox<Role> tagComboBox = new TagComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		tagComboBox.setModel(query ->
				query == null || query.isBlank() ?
						isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.getAll().stream().limit(50).collect(Collectors.toList()) :
						isAppFilter() ? Role.filter().parseFullTextFilter(query).organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.filter().parseFullTextFilter(query).execute()
		);
		PropertyProvider<Role> propertyProvider = PropertyProviders.createRolePropertyProvider(getApplicationInstanceData());
		tagComboBox.setPropertyProvider(propertyProvider);
		tagComboBox.setRecordToStringFunction(unitType -> (String) propertyProvider.getValues(unitType, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION));
		tagComboBox.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		tagComboBox.setDistinct(true);
		return tagComboBox;
	}

	private TagComboBox<OrganizationUnitType> createOrgUnitTypeTagComboBox() {
		TagComboBox<OrganizationUnitType> tagComboBox = new TagComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		tagComboBox.setModel(query -> query == null || query.isBlank() ?
				OrganizationUnitType.getAll().stream().limit(50).collect(Collectors.toList()) :
				OrganizationUnitType.filter().parseFullTextFilter(query).execute().stream().limit(50).collect(Collectors.toList())
		);
		PropertyProvider<OrganizationUnitType> propertyProvider = PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData());
		tagComboBox.setPropertyProvider(propertyProvider);
		tagComboBox.setRecordToStringFunction(unitType -> (String) propertyProvider.getValues(unitType, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION));
		tagComboBox.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		tagComboBox.setDistinct(true);
		return tagComboBox;

	}


}

