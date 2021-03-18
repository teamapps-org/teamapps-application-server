package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.localize.TranslatableField;
import org.teamapps.application.server.ux.localize.TranslatableTextUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.OrganizationField;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

public class OrganizationFieldPerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<OrganizationField> selectedField = TwoWayBindableValue.create();

	public OrganizationFieldPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.CONSOLE, getLocalized("organizationField.organizationFields"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.CONSOLE, getLocalized("organizationField.organizationField"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<OrganizationField> orgFieldModelBuilder = new EntityModelBuilder<>(OrganizationField::filter, getApplicationInstanceData());
		orgFieldModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("organizationField.organizationFields"));
		orgFieldModelBuilder.attachSearchField(masterView);
		orgFieldModelBuilder.onSelectedRecordChanged.addListener(selectedField::set);
		Table<OrganizationField> table = orgFieldModelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_LARGE_ICON_SINGLE_LINE, PropertyProviders.createOrganizationFieldPropertyProvider(getApplicationInstanceData()), 38);
		orgFieldModelBuilder.updateModels();
		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		TranslatableField translatableNameField = TranslatableTextUtils.createTranslatableField(getApplicationInstanceData());
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, true);
		iconComboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);

		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("organizationField.organizationFieldName"), translatableNameField);
		formLayout.addLabelAndField(null, getLocalized("organizationField.icon"), iconComboBox);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> selectedField.set(OrganizationField.create()));

		saveButton.onClick.addListener(() -> {
			OrganizationField field = selectedField.get();
			if (field != null && translatableNameField.getValue() != null && iconComboBox.getValue() != null) {
				field
						.setTitle(translatableNameField.getValue())
						.setIcon(IconUtils.encodeNoStyle(iconComboBox.getValue()))
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				orgFieldModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedField.onChanged().addListener(type -> {
			translatableNameField.setValue(type.getTitle());
			iconComboBox.setValue(IconUtils.decodeIcon(type.getIcon()));
		});

	}


}

