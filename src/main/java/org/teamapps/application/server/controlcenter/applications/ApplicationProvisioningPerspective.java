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
package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.system.utils.ApplicationUiUtils;
import org.teamapps.application.server.ui.localize.LocalizationTranslationKeyField;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.application.server.ui.localize.LocalizationKeyWindow;
import org.teamapps.application.server.ui.localize.LocalizationUiUtils;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.combo.RecordComboBox;
import org.teamapps.application.ux.form.FormPanel;
import org.teamapps.application.ux.form.FormWindow;
import org.teamapps.application.tools.EntityListModelBuilder;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.CheckBox;
import org.teamapps.ux.component.field.Fields;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.linkbutton.LinkButton;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationProvisioningPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<ManagedApplication> selectedApplication = TwoWayBindableValue.create();


	public ApplicationProvisioningPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View applicationsView = View.createView(StandardLayout.CENTER, ApplicationIcons.INSTALL, getLocalized("applicationProvisioning.provisionedApplications"), null);
		View applicationDetailsView = View.createView(StandardLayout.RIGHT, ApplicationIcons.INSTALL, getLocalized("applicationProvisioning.provisionedApplication"), null);
		applicationDetailsView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<ManagedApplication> applicationModelBuilder = new EntityModelBuilder<>(ManagedApplication::filter, getApplicationInstanceData());
		Table<ManagedApplication> applicationsTable = applicationModelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES, PropertyProviders.createManagedApplicationPropertyProvider(userSessionData), 60);
		applicationsTable.setStripedRows(false);
		applicationsView.setComponent(applicationsTable);
		applicationModelBuilder.onDataChanged.fire();
		applicationModelBuilder.attachSearchField(applicationsView);
		applicationModelBuilder.attachViewCountHandler(applicationsView, () -> getLocalized("applicationProvisioning.provisionedApplications"));
		applicationModelBuilder.onSelectedRecordChanged.addListener(selectedApplication::set);

		getPerspective().addView(applicationsView);
		getPerspective().addView(applicationDetailsView);

		ResponsiveForm<?> applicationForm = new ResponsiveForm<>(100, 150, 0);
		ResponsiveFormLayout formLayout = applicationForm.addResponsiveFormLayout(400);

		ComboBox<Application> applicationComboBox = ApplicationUiUtils.createApplicationComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, userSessionData);
		ComboBox<OrganizationField> organizationFieldCombo = OrganizationUtils.createOrganizationFieldCombo(getApplicationInstanceData());
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_SINGLE_LINE, true);
		iconComboBox.setShowClearButton(true);

		LocalizationTranslationKeyField titleKeyField = new LocalizationTranslationKeyField(getLocalized("applications.createNewTitle"), getApplicationInstanceData(), applicationComboBox::getValue);
		LocalizationTranslationKeyField descriptionKeyField = new LocalizationTranslationKeyField(getLocalized("applications.createNewDescription"), getApplicationInstanceData(), applicationComboBox::getValue);

		CheckBox darkThemeCheckBox = new CheckBox(getLocalized("applications.darkTheme"));
		CheckBox toolbarAppMenuCheckbox = new CheckBox(getLocalized("applications.useToolbarApplicationMenu"));

		EntityListModelBuilder<ManagedApplicationPerspective> perspectiveModelBuilder = new EntityListModelBuilder<>(getApplicationInstanceData());
		Table<ManagedApplicationPerspective> perspectivesList = perspectiveModelBuilder.createTable();
		perspectivesList.setDisplayAsList(true);
		perspectivesList.setForceFitWidth(true);
		perspectivesList.setRowHeight(26);
		TemplateField<ManagedApplicationPerspective> perspectiveColumnField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPerspectivePropertyProvider(userSessionData));
		TemplateField<Application> applicationColumnField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createApplicationPropertyProvider(userSessionData));

		perspectivesList.addColumn(new TableColumn<>("perspective", getLocalized("applications.perspective"), perspectiveColumnField));
		perspectivesList.addColumn(new TableColumn<>("application", getLocalized("applications.application"), applicationColumnField));
		perspectivesList.setPropertyExtractor((record, propertyName) -> switch (propertyName){
			case "perspective" -> record;
			case "application" -> record.getApplicationPerspective() != null ? record.getApplicationPerspective().getApplication() : null;
			default -> null;
		});

		perspectivesList.setStripedRows(false);
		FormPanel formPanel = new FormPanel(getApplicationInstanceData());
		formPanel.setTable(perspectivesList, true, true, true);
		formPanel.addButtonGroup();
		ToolbarButton moveUpButton = formPanel.addButton(ApplicationIcons.NAVIGATE_UP, getLocalized("applications.moveUp"));
		ToolbarButton moveDownButton = formPanel.addButton(ApplicationIcons.NAVIGATE_DOWN, getLocalized("applications.moveDown"));

		CheckBox hideApplicationCheckBox = new CheckBox(getLocalized(Dictionary.HIDE));
		ComboBox<ManagedApplicationGroup> applicationGroupComboBox = ApplicationUiUtils.createApplicationGroupComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, getApplicationInstanceData());

		Arrays.asList(applicationComboBox, iconComboBox, titleKeyField.getKeyDisplayField(), descriptionKeyField.getKeyDisplayField(), applicationGroupComboBox).forEach(f -> f.setRequired(true));

		formLayout.addSection().setDrawHeaderLine(false).setCollapsible(false);
		formLayout.addLabelAndComponent(null, getLocalized("applications.mainApplication"), applicationComboBox);
		formLayout.addLabelAndComponent(null, getLocalized(Dictionary.HIDDEN), hideApplicationCheckBox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.organizationField"), organizationFieldCombo);
		formLayout.addLabelAndComponent(null, getLocalized("applications.applicationIcon"), iconComboBox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.appTitle"), titleKeyField.getSelectionField());
		formLayout.addLabelAndComponent(null, getLocalized("applications.appTitle"), titleKeyField.getKeyDisplayField());
		formLayout.addLabelAndComponent(null, null, titleKeyField.getKeyLinkButton());
		formLayout.addLabelAndComponent(null, getLocalized("applications.appDescription"), descriptionKeyField.getSelectionField());
		formLayout.addLabelAndComponent(null, null, descriptionKeyField.getKeyLinkButton());
		formLayout.addLabelAndComponent(null, getLocalized("applications.darkTheme"), darkThemeCheckBox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.toolbarAppMenu"), toolbarAppMenuCheckbox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.perspectives"), formPanel.getPanel());
		formLayout.addLabelAndComponent(null, getLocalized("applications.applicationGroup"), applicationGroupComboBox);

		ToolbarButtonGroup buttonGroup = applicationDetailsView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addApplicationButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized("applications.addApplication"), getLocalized("applications.addApplication.desc")));

		buttonGroup = applicationDetailsView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveApplicationButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		formPanel.getAddButton().onClick.addListener(() -> showPerspectiveFormWindow(null, perspectiveModelBuilder));
		formPanel.getEditButton().onClick.addListener(() -> showPerspectiveFormWindow(perspectivesList.getSelectedRecord(), perspectiveModelBuilder));
		formPanel.getDeleteButton().onClick.addListener(() -> perspectiveModelBuilder.removeRecord(perspectivesList.getSelectedRecord()));
		addApplicationButton.onClick.addListener(() -> selectedApplication.set(ManagedApplication.create()));

		saveApplicationButton.onClick.addListener(() -> {
			ManagedApplication application = selectedApplication.get();
			if (application == null) {
				return;
			}
			if (Fields.validateAll(applicationComboBox, iconComboBox, titleKeyField.getSelectionField(), descriptionKeyField.getSelectionField(), applicationGroupComboBox) && (!perspectiveModelBuilder.getRecords().isEmpty() || application.isSingleApplication() || application.getMainApplication().isUnmanagedApplication())) {
				Application mainApplication = applicationComboBox.getValue();
				application.setMainApplication(mainApplication);
				application.setHidden(hideApplicationCheckBox.getValue());
				application.setOrganizationField(organizationFieldCombo.getValue());
				application.setIcon(mainApplication.getIcon().equals(IconUtils.encodeNoStyle(iconComboBox.getValue())) ? null : IconUtils.encodeNoStyle(iconComboBox.getValue()));
				application.setTitleKey(mainApplication.getTitleKey().equals(titleKeyField.getKey()) ? null : titleKeyField.getKey());
				application.setDescriptionKey(mainApplication.getDescriptionKey().equals(descriptionKeyField.getKey()) ? null : descriptionKeyField.getKey());
				application.setDarkTheme(darkThemeCheckBox.getValue());
				application.setToolbarApplicationMenu(toolbarAppMenuCheckbox.getValue());
				application.setPerspectives(perspectiveModelBuilder.getRecords());
				application.setApplicationGroup(applicationGroupComboBox.getValue());
				application.save();
				int pos = 0;
				for (ManagedApplicationPerspective perspective : perspectiveModelBuilder.getRecords()) {
					perspective.setListingPosition(pos++).save();
				}
				applicationModelBuilder.onDataChanged.fire();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		moveUpButton.onClick.addListener(() -> changePerspectiveOrder(perspectiveModelBuilder, perspectivesList, true));
		moveDownButton.onClick.addListener(() -> changePerspectiveOrder(perspectiveModelBuilder, perspectivesList, false));

		applicationComboBox.onValueChanged.addListener(app -> {
			iconComboBox.setValue(app != null ? IconUtils.decodeIcon(app.getIcon()) : null);
			titleKeyField.setKey(app != null ? app.getTitleKey() : null);
			descriptionKeyField.setKey(app != null ? app.getDescriptionKey() : null);
			if (selectedApplication.get() != null && !selectedApplication.get().isStored()) {
				List<ManagedApplicationPerspective> perspectives = app.getPerspectives().stream().filter(ApplicationPerspective::getAutoProvision).map(p -> ManagedApplicationPerspective.create().setApplicationPerspective(p)).collect(Collectors.toList());
				perspectiveModelBuilder.setRecords(perspectives);
			}
		});

		selectedApplication.onChanged().addListener(app -> {
			applicationComboBox.setValue(app.getMainApplication());
			hideApplicationCheckBox.setValue(app.getHidden());
			organizationFieldCombo.setValue(app.getOrganizationField());
			iconComboBox.setValue(app.getIcon() != null ? IconUtils.decodeIcon(app.getIcon()) : app.getMainApplication() != null ? IconUtils.decodeIcon(app.getMainApplication().getIcon()) : null);
			titleKeyField.setKey(app.getTitleKey() != null ? app.getTitleKey() : app.getMainApplication() != null ? app.getMainApplication().getTitleKey() : null);
			descriptionKeyField.setKey(app.getDescriptionKey() != null ? app.getDescriptionKey() : app.getMainApplication() != null ? app.getMainApplication().getDescriptionKey() : null);
			darkThemeCheckBox.setValue(app.getDarkTheme());
			toolbarAppMenuCheckbox.setValue(app.getToolbarApplicationMenu());
			perspectiveModelBuilder.setRecords(app.getPerspectives().stream().filter(perspective -> perspective.getApplicationPerspective() != null).sorted((Comparator.comparingInt(ManagedApplicationPerspective::getListingPosition))).collect(Collectors.toList()));
			applicationGroupComboBox.setValue(app.getApplicationGroup());
		});
		applicationDetailsView.setComponent(applicationForm);
		selectedApplication.set(ManagedApplication.create());
	}

	private void showPerspectiveFormWindow(ManagedApplicationPerspective managedApplicationPerspective, EntityListModelBuilder<ManagedApplicationPerspective> perspectiveModelBuilder) {
		ManagedApplicationPerspective perspective = managedApplicationPerspective != null ? managedApplicationPerspective : ManagedApplicationPerspective.create();
		FormWindow formWindow = new FormWindow(ApplicationIcons.WINDOWS, getLocalized("applications.perspective"), getApplicationInstanceData());
		formWindow.addSaveButton();
		formWindow.addCancelButton();
		formWindow.addSection();

		ComboBox<Application> applicationComboBox = ApplicationUiUtils.createApplicationComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, userSessionData);
		RecordComboBox<ApplicationPerspective> applicationsPerspectiveCombo = new RecordComboBox<>(PropertyProviders.createApplicationPerspectivePropertyProvider(userSessionData), BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);

		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_SINGLE_LINE, true);
		ComboBox<String> titleKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, getApplicationInstanceData(), applicationComboBox::getValue);
		LinkButton crateTitleKeyButton = new LinkButton(getLocalized("applications.createNewTitle"));
		ComboBox<String> descriptionKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, getApplicationInstanceData(), applicationComboBox::getValue);
		LinkButton createDescriptionKeyButton = new LinkButton(getLocalized("applications.createNewDescription"));
		CheckBox toolbarPerspectiveMenuCheckBox = new CheckBox(getLocalized("applications.useToolbarPerspectiveMenu"));


		formWindow.addField(getLocalized("applications.application"), applicationComboBox);
		formWindow.addField(getLocalized("applications.perspective"), applicationsPerspectiveCombo);
		formWindow.addField(getLocalized("applications.perspectiveIcon"), iconComboBox);
		formWindow.addField(getLocalized("applications.perspectiveTitle"), titleKeyCombo);
		formWindow.addField(null, crateTitleKeyButton);
		formWindow.addField(getLocalized("applications.perspectiveDescription"), descriptionKeyCombo);
		formWindow.addField(null, createDescriptionKeyButton);
		formWindow.addField(getLocalized("applications.toolbarPerspectiveMenu"), toolbarPerspectiveMenuCheckBox);

		if (managedApplicationPerspective != null) {
			applicationComboBox.setValue(perspective.getApplicationPerspective().getApplication());
			applicationsPerspectiveCombo.setValue(perspective.getApplicationPerspective());
			iconComboBox.setValue(perspective.getIconOverride() != null ? IconUtils.decodeIcon(perspective.getIconOverride()) : IconUtils.decodeIcon(perspective.getApplicationPerspective().getIcon()));
			titleKeyCombo.setValue(perspective.getTitleKeyOverride() != null ? perspective.getTitleKeyOverride() : perspective.getApplicationPerspective().getTitleKey());
			descriptionKeyCombo.setValue(perspective.getDescriptionKeyOverride() != null ? perspective.getDescriptionKeyOverride() : perspective.getApplicationPerspective().getDescriptionKey());
			toolbarPerspectiveMenuCheckBox.setValue(perspective.getToolbarPerspectiveMenu());
		}

		Arrays.asList(applicationsPerspectiveCombo, iconComboBox, titleKeyCombo, descriptionKeyCombo, applicationsPerspectiveCombo).forEach(f -> f.setRequired(true));

		applicationComboBox.onValueChanged.addListener(app -> applicationsPerspectiveCombo.setRecords(app.getPerspectives()));
		applicationsPerspectiveCombo.onValueChanged.addListener(p -> {
			iconComboBox.setValue(p != null ? IconUtils.decodeIcon(p.getIcon()) : null);
			titleKeyCombo.setValue(p != null ? p.getTitleKey() : null);
			descriptionKeyCombo.setValue(p != null ? p.getDescriptionKey() : null);
		});

		formWindow.getSaveButton().onClick.addListener(() -> {
			if (Fields.validateAll(applicationsPerspectiveCombo, iconComboBox, titleKeyCombo, descriptionKeyCombo, applicationsPerspectiveCombo)) {
				ApplicationPerspective applicationPerspective = applicationsPerspectiveCombo.getValue();
				perspective.setApplicationPerspective(applicationPerspective);
				perspective.setIconOverride(applicationPerspective.getIcon().equals(iconComboBox.getValue()) ? null : IconUtils.encodeNoStyle(iconComboBox.getValue()));
				perspective.setTitleKeyOverride(applicationPerspective.getTitleKey().equals(titleKeyCombo.getValue()) ? null : titleKeyCombo.getValue());
				perspective.setDescriptionKeyOverride(applicationPerspective.getDescriptionKey().equals(descriptionKeyCombo.getValue()) ? null : descriptionKeyCombo.getValue());
				perspective.setToolbarPerspectiveMenu(toolbarPerspectiveMenuCheckBox.getValue());
				perspective.save();
				if (managedApplicationPerspective == null) {
					perspectiveModelBuilder.addRecord(perspective);
				}
				formWindow.close();
			}
		});

		formWindow.show();
	}

	private void changePerspectiveOrder(EntityListModelBuilder<ManagedApplicationPerspective> perspectiveModelBuilder, Table<ManagedApplicationPerspective> perspectivesList, boolean moveUp) {
		ManagedApplicationPerspective selectedPerspective = perspectivesList.getSelectedRecord();
		if (selectedPerspective != null) {
			List<ManagedApplicationPerspective> perspectives = perspectiveModelBuilder.getRecords();
			int position = 0;
			int changeValue = moveUp ? -5 : 15;
			for (ManagedApplicationPerspective perspective : perspectives) {
				if (perspective.equals(selectedPerspective)) {
					perspective.setListingPosition(position + changeValue);
				} else {
					position += 10;
					perspective.setListingPosition(position);
				}
			}
			perspectiveModelBuilder.setRecords(perspectives.stream().sorted((Comparator.comparingInt(ManagedApplicationPerspective::getListingPosition))).collect(Collectors.toList()));
		}
	}


}
