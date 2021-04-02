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
package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Country;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.application.PerspectiveMenuPanel;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.address.AddressForm;
import org.teamapps.application.server.ux.localize.TranslatableField;
import org.teamapps.application.server.ux.localize.TranslatableTextUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.GeoLocationType;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrganizationPerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<OrganizationUnit> selectedUnit = TwoWayBindableValue.create();

	public OrganizationPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		OrganizationPerspectiveBuilder organizationPerspectiveBuilder = new OrganizationPerspectiveBuilder();
		PerspectiveMenuPanel menuPanel = PerspectiveMenuPanel.createMenuPanel(getApplicationInstanceData(),
				new OrganizationChartPerspectiveBuilder(),
				organizationPerspectiveBuilder,
				new OrganizationUnitTypePerspectiveBuilder(),
				new OrganizationFieldPerspectiveBuilder()
		);
		menuPanel.addInstantiatedPerspective(organizationPerspectiveBuilder, this);
		setPerspectiveMenuPanel(menuPanel.getComponent());

		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.ELEMENTS_HIERARCHY, getLocalized("organization.organizationUnits"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.ELEMENTS_HIERARCHY, getLocalized("organization.organizationUnit"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<OrganizationUnit> orgUnitModelBuilder = new EntityModelBuilder<>(OrganizationUnit::filter, getApplicationInstanceData());
		orgUnitModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("organization.organizationUnits"));
		orgUnitModelBuilder.attachSearchField(masterView);
		orgUnitModelBuilder.onSelectedRecordChanged.addListener(selectedUnit::set);
		Tree<OrganizationUnit> tree = orgUnitModelBuilder.createTree(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, PropertyProviders.creatOrganizationUnitPropertyProvider(getApplicationInstanceData()), OrganizationUnit::getParent, unit -> OrganizationUtils.getLevel(unit) < 2);
		orgUnitModelBuilder.updateModels();
		masterView.setComponent(tree);

		//printUnits(OrganizationUnit.getById(1), 0);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		TemplateField<OrganizationUnit> parentUnitField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitPropertyProvider(getApplicationInstanceData()));
		TranslatableField translatableNameField = TranslatableTextUtils.createTranslatableField(getApplicationInstanceData());
		ComboBox<OrganizationUnitType> unitTypeComboBox = createOrgUnitTypeComboBox();
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox();
		AddressForm addressForm = new AddressForm(getApplicationInstanceData());
		addressForm.setWithName(true);
		addressForm.setWithGeoCoordinates(true);

		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection(ApplicationIcons.FOLDER, getLocalized(""));
		formLayout.addLabelAndField(null, getLocalized("organizationUnit.parentUnit"), parentUnitField);
		formLayout.addLabelAndField(null, getLocalized("organizationUnit.unitName"), translatableNameField);
		formLayout.addLabelAndField(null, getLocalized("organizationUnit.unitType"), unitTypeComboBox);
		formLayout.addLabelAndField(null, getLocalized("organizationUnit.icon"), iconComboBox);

		addressForm.createAddressSection(formLayout);
		addressForm.addFields(formLayout);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> {
			OrganizationUnit organizationUnit = selectedUnit.get();
			if (organizationUnit == null && OrganizationUnit.getCount() > 0) {
				UiUtils.showNotification(ApplicationIcons.ERROR, getLocalized("organizationUnit.errorNoOrgUnitSelected"));
				return;
			}
			selectedUnit.set(OrganizationUnit.create().setParent(organizationUnit));
		});

		saveButton.onClick.addListener(() -> {
			OrganizationUnit unit = selectedUnit.get();
			if (unit == null) {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
				return;
			}
			OrganizationUnit parentUnit = unit.getParent();
			if (translatableNameField.getValue() != null && unitTypeComboBox.getValue() != null && addressForm.validateAddress() && (parentUnit != null || OrganizationUnit.getCount() == 0)) {
				unit
						.setParent(parentUnit)
						.setName(translatableNameField.getValue())
						.setType(unitTypeComboBox.getValue())
						.setIcon(IconUtils.encodeNoStyle(iconComboBox.getValue()))
						.setAddress(addressForm.getAddress())
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				orgUnitModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedUnit.onChanged().addListener(unit -> {
			parentUnitField.setValue(unit.getParent());
			translatableNameField.setValue(unit.getName());
			unitTypeComboBox.setValue(unit.getType());
			iconComboBox.setValue(IconUtils.decodeIcon(unit.getIcon()));
			addressForm.setAddress(unit.getAddress());
			if (!unit.isStored()) {
				if (unit.getParent() != null) {
					unitTypeComboBox.setValue(unit.getParent().getType().getDefaultChildType());
				}

				if (OrganizationUtils.getParentWithGeoType(unit, GeoLocationType.COUNTRY) != null) {
					OrganizationUnit countryParent = OrganizationUtils.getParentWithGeoType(unit, GeoLocationType.COUNTRY);
					if (countryParent.getAddress() != null) {
						addressForm.getCountryComboBox().setValue(Country.getCountryByIsoCode(countryParent.getAddress().getCountry()));
					}
				}
			}
		});

		if (OrganizationUnit.getCount() == 0) {
			selectedUnit.set(OrganizationUnit.create().setParent(null));
		}
	}

	private void printUnits(OrganizationUnit unit, int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append("\t");
		}
		String space = sb.toString();
		System.out.println(space + unit.getName().getText() + " (" + unit.getType().getName().getText() + ")");
		for (OrganizationUnit child : unit.getChildren()) {
			printUnits(child, level + 1);
		}
	}

	private ComboBox<OrganizationUnitType> createOrgUnitTypeComboBox() {
		ComboBox<OrganizationUnitType> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		PropertyProvider<OrganizationUnitType> propertyProvider = PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData());
		comboBox.setModel(query -> {
			List<OrganizationUnitType> allowedTypes = selectedUnit.get() != null && selectedUnit.get().getParent() != null ? selectedUnit.get().getParent().getType().getPossibleChildrenTypes() : OrganizationUnitType.getAll();
			if (query == null || query.isBlank()) {
				return allowedTypes;
			} else {
				String q = query.toLowerCase();
				return allowedTypes.stream()
						.filter(type -> ((String) propertyProvider.getValues(type, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION)).toLowerCase().contains(q))
						.collect(Collectors.toList());
			}
		});
		comboBox.setPropertyProvider(propertyProvider);
		comboBox.setRecordToStringFunction(unitType -> (String) propertyProvider.getValues(unitType, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION));
		return comboBox;
	}

}

