package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.combo.ComboBoxUtils;
import org.teamapps.application.server.ux.localize.TranslatableField;
import org.teamapps.application.server.ux.localize.TranslatableTextUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.GeoLocationType;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.universaldb.index.translation.TranslatableText;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OrganizationUnitTypePerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<OrganizationUnitType> selectedType = TwoWayBindableValue.create();


	public OrganizationUnitTypePerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.CONSOLE, getLocalized("organizationUnitType.organizationUnitTypes"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.CONSOLE, getLocalized("organizationUnitType.organizationUnitType"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<OrganizationUnitType> orgUnitModelBuilder = new EntityModelBuilder<>(OrganizationUnitType::filter, getApplicationInstanceData());
		orgUnitModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("organizationUnitType.organizationUnitTypes"));
		orgUnitModelBuilder.attachSearchField(masterView);
		orgUnitModelBuilder.onSelectedRecordChanged.addListener(selectedType::set);
		Table<OrganizationUnitType> table = orgUnitModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(28);
		table.setStripedRows(false);
		orgUnitModelBuilder.updateModels();

		TemplateField<OrganizationUnitType> unitTypeField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData()));
		TemplateField<TranslatableText> abbreviationField = TranslatableTextUtils.createTranslatableTemplateField(getApplicationInstanceData());
		TemplateField<OrganizationUnitType> defaultChildField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData()));
		TagComboBox<OrganizationUnitType> allowedChildTypesField = UiUtils.createTagComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData()));

		table.addColumn(new TableColumn<OrganizationUnitType>(OrganizationUnitType.FIELD_NAME, getLocalized("organizationUnitType.organizationUnitType"), unitTypeField).setDefaultWidth(170));
		table.addColumn(new TableColumn<OrganizationUnitType>(OrganizationUnitType.FIELD_ABBREVIATION, getLocalized("organizationUnitType.abbreviation"), abbreviationField).setDefaultWidth(80));
		table.addColumn(new TableColumn<OrganizationUnitType>(OrganizationUnitType.FIELD_DEFAULT_CHILD_TYPE, getLocalized("organizationUnitType.defaultChildType"), defaultChildField).setDefaultWidth(130));
		table.addColumn(new TableColumn<OrganizationUnitType>(OrganizationUnitType.FIELD_POSSIBLE_CHILDREN_TYPES, getLocalized("organizationUnitType.allowedChildrenTypes"), allowedChildTypesField).setDefaultWidth(350));

		table.setPropertyExtractor((unitType, propertyName) -> switch (propertyName) {
			case OrganizationUnitType.FIELD_NAME -> unitType;
			case OrganizationUnitType.FIELD_ABBREVIATION -> unitType.getAbbreviation();
			case OrganizationUnitType.FIELD_DEFAULT_CHILD_TYPE -> unitType.getDefaultChildType();
			case OrganizationUnitType.FIELD_POSSIBLE_CHILDREN_TYPES -> unitType.getPossibleChildrenTypes();
			default -> null;
		});

		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		TranslatableField translatableNameField = TranslatableTextUtils.createTranslatableField(getApplicationInstanceData());
		TranslatableField translatableAbbreviationField = TranslatableTextUtils.createTranslatableField(getApplicationInstanceData());
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, true);
		iconComboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		CheckBox translateOrgUnitsCheckBox = new CheckBox(getLocalized("organizationUnitType.translateOrganizationUnits"));
		CheckBox allowAsUserContainerCheckBox = new CheckBox(getLocalized("organizationUnitType.allowAsUserContainer"));
		ComboBox<OrganizationUnitType> defaultChildTypeCombo = createOrgUnitTypeComboBox();
		TagComboBox<OrganizationUnitType> possibleChildrenTagCombo = createOrgUnitTypeTagComboBox();
		ComboBox<GeoLocationType> geoLocationComboBox = createGeoLocationComboBox();

		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.typeTitle"), translatableNameField);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.abbreviation"), translatableAbbreviationField);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.icon"), iconComboBox);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.translateUnits"), translateOrgUnitsCheckBox);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.allowUsers"), allowAsUserContainerCheckBox);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.defaultChildType"), defaultChildTypeCombo);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.allowedChildrenTypes"), possibleChildrenTagCombo);
		formLayout.addLabelAndField(null, getLocalized("organizationUnitType.geoLocationType"), geoLocationComboBox);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> selectedType.set(OrganizationUnitType.create()));

		saveButton.onClick.addListener(() -> {
			OrganizationUnitType type = selectedType.get();
			if (type != null && translatableNameField.getValue() != null && geoLocationComboBox.getValue() != null) {
				type
						.setName(translatableNameField.getValue())
						.setAbbreviation(translatableAbbreviationField.getValue())
						.setIcon(IconUtils.encodeNoStyle(iconComboBox.getValue()))
						.setTranslateOrganizationUnits(translateOrgUnitsCheckBox.getValue())
						.setAllowUserContainer(allowAsUserContainerCheckBox.getValue())
						.setDefaultChildType(defaultChildTypeCombo.getValue())
						.setPossibleChildrenTypes(possibleChildrenTagCombo.getValue())
						.setGeoLocationType(geoLocationComboBox.getValue())
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				orgUnitModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedType.onChanged().addListener(type -> {
			translatableNameField.setValue(type.getName());
			translatableAbbreviationField.setValue(type.getAbbreviation());
			iconComboBox.setValue(IconUtils.decodeIcon(type.getIcon()));
			translateOrgUnitsCheckBox.setValue(type.getTranslateOrganizationUnits());
			allowAsUserContainerCheckBox.setValue(type.getAllowUserContainer());
			defaultChildTypeCombo.setValue(type.getDefaultChildType());
			possibleChildrenTagCombo.setValue(type.getPossibleChildrenTypes());
			geoLocationComboBox.setValue(type.getGeoLocationType());
		});

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

	private ComboBox<OrganizationUnitType> createOrgUnitTypeComboBox() {
		ComboBox<OrganizationUnitType> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setModel(query -> query == null || query.isBlank() ?
				OrganizationUnitType.getAll().stream().limit(50).collect(Collectors.toList()) :
				OrganizationUnitType.filter().parseFullTextFilter(query).execute().stream().limit(50).collect(Collectors.toList())
		);
		PropertyProvider<OrganizationUnitType> propertyProvider = PropertyProviders.creatOrganizationUnitTypePropertyProvider(getApplicationInstanceData());
		comboBox.setPropertyProvider(propertyProvider);
		comboBox.setRecordToStringFunction(unitType -> (String) propertyProvider.getValues(unitType, Collections.emptyList()).get(BaseTemplate.PROPERTY_CAPTION));
		return comboBox;
	}

	private ComboBox<GeoLocationType> createGeoLocationComboBox() {
		PropertyProvider<GeoLocationType> propertyProvider = (type, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, type != GeoLocationType.NONE ? ApplicationIcons.MAP_LOCATION: ApplicationIcons.SIGN_FORBIDDEN);
			map.put(BaseTemplate.PROPERTY_CAPTION, getLocalized("organizationUnitType.geoLocationType.") + type.name());
			return map;
		};
		return ComboBoxUtils.createRecordComboBox(Arrays.asList(GeoLocationType.values()), propertyProvider, BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
	}

}
