package org.teamapps.application.server.controlcenter.translations;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.ux.combo.ComboBoxUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.common.format.RgbaColor;
import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.icons.composite.CompositeIcon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.MultiLineTextField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.format.Spacing;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

import java.util.*;
import java.util.function.Predicate;

public class TranslationsPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<LocalizationKey> selectedKey = TwoWayBindableValue.create();

	private String currentLanguage;
	private String currentTemplate1;
	private String currentTemplate2;

	private boolean language1Visible;
	private boolean language2Visible;
	private boolean machineTranslationVisible;


	public TranslationsPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View localizationKeyView = View.createView(StandardLayout.CENTER, ApplicationIcons.DOCUMENT_TEXT, getLocalized("translations.overView"), null);
		View topicImageView = View.createView(StandardLayout.CENTER_BOTTOM, ApplicationIcons.FORM, getLocalized(Dictionary.PREVIEW_IMAGE), null);
		View translationView = View.createView(StandardLayout.RIGHT, ApplicationIcons.DOCUMENT_TEXT, getLocalized("translations.translation"), null);
		localizationKeyView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));
		translationView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		topicImageView.setVisible(false);

		ToolbarButtonGroup buttonGroup = localizationKeyView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton overViewButtonOn = buttonGroup.addButton(ToolbarButton.create(CompositeIcon.of(ApplicationIcons.DOCUMENT_TEXT, ApplicationIcons.CHECKBOX), getLocalized("translations.overView"), getLocalized("translations.showOverView")));
		ToolbarButton overViewButtonOff = buttonGroup.addButton(ToolbarButton.create(CompositeIcon.of(ApplicationIcons.DOCUMENT_TEXT, ApplicationIcons.DELETE), getLocalized("translations.overView"), getLocalized("translations.hideOverView")));
		overViewButtonOn.setVisible(false);
		/*
			Main toolbar:
			Left: Overview (on/off), Preview image (on/off),
			Right: Template 1 (on/off), Template 2 (..), Auto-Translation (..), [Proofread, Adm: Translation], [Admin: Proofread, Topic, Admin local, Admin full]
		 */

		/*
			Translation toolbar:
			Previous, Next
			Translation mode:  Done, Unclear, Copy auto translation (done: status != verified, text not empty; unclear: notes not empty)
			Proofreading mode: Verified, Incorrect
			Administration mode: Save
		 */

		buttonGroup = translationView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton previousButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.NAVIGATE_LEFT, getLocalized(Dictionary.PREVIOUS)));
		ToolbarButton nextButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.NAVIGATE_RIGHT, getLocalized(Dictionary.NEXT)));

		buttonGroup = translationView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton doneButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.CHECKBOX, getLocalized(Dictionary.DONE)));
		ToolbarButton unclearButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.DELETE, getLocalized(Dictionary.UNCLEAR)));

		buttonGroup = translationView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton copyTranslationButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.COPY, getLocalized("translations.copyTranslation")));

		ComboBox<Language> languageCombo = Language.createComboBox(getApplicationInstanceData());
		ComboBox<Language> template1Combo = Language.createComboBox(getApplicationInstanceData());
		ComboBox<Language> template2Combo = Language.createComboBox(getApplicationInstanceData());

		ComboBox<TranslationWorkState> workStateComboBox = createWorkStateComboBox();
		ComboBox<LocalizationTopic> topicComboBox = createTopicComboBox();
		ComboBox<TranslationMode> modeComboBox = createTranslationModeComboBox();

		workStateComboBox.setValue(TranslationWorkState.ALL);
		modeComboBox.setValue(TranslationMode.TRANSLATE);
		languageCombo.setValue(Language.FR_FRENCH);
		template1Combo.setValue(Language.EN_ENGLISH);
		template2Combo.setValue(Language.DE_GERMAN);

		currentLanguage = "fr";
		currentTemplate1 = "en";
		currentTemplate2 = "de";


		ResponsiveForm selectionForm = new ResponsiveForm(50, 75, 200);
		selectionForm.setMargin(Spacing.px(0));
		ResponsiveFormLayout formLayout = selectionForm.addResponsiveFormLayout(500);
		formLayout.addSection().setCollapsible(false).setPadding(new Spacing(0, 5)).setMargin(new Spacing(4, 2, 4, 2));

		formLayout.addLabelAndField(null, getLocalized("translations.language"), languageCombo);
		formLayout.addLabelAndField(null, getLocalized("translations.status"), workStateComboBox, false);
		formLayout.addLabelAndField(null, getLocalized("translations.template1"), template1Combo);
		formLayout.addLabelAndField(null, getLocalized("translations.topic"), topicComboBox, false);
		formLayout.addLabelAndField(null, getLocalized("translations.template2"), template2Combo);
		formLayout.addLabelAndField(null, getLocalized("translations.mode"), modeComboBox, false);

		EntityModelBuilder<LocalizationKey> keyModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? LocalizationKey.filter().application(NumericFilter.equalsFilter(getMainApplication().getId())) : LocalizationKey.filter(), getApplicationInstanceData());
		keyModelBuilder.updateModels();
		keyModelBuilder.attachSearchField(localizationKeyView);
		keyModelBuilder.attachViewCountHandler(localizationKeyView, () -> getLocalized("translations.overView"));
		keyModelBuilder.onSelectedRecordChanged.addListener(key -> selectedKey.set(key));
		Table<LocalizationKey> keyTable = keyModelBuilder.createTable();
		keyTable.setDisplayAsList(true);

		keyTable.setCssStyle("background-color", "white");
		keyTable.setCssStyle("border-top", "1px solid " + Color.MATERIAL_GREY_400.toHtmlColorString());
		keyTable.setRowHeight(28);

		keyTable.setPropertyProvider((key, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			Map<String, LocalizationValue> valueMap = TranslationUtils.getValueMap(key);
			map.put("key", key);
			map.put("language", valueMap.get(currentLanguage));
			map.put("status", valueMap.get(currentLanguage));
			map.put("template1", valueMap.get(currentTemplate1));
			map.put("template2", valueMap.get(currentTemplate2));
			return map;
		});

		TemplateField<LocalizationValue> languageColumn = createLocalizationValueTemplateField(false, false);
		TemplateField<LocalizationValue> template1Column = createLocalizationValueTemplateField(false, false);
		TemplateField<LocalizationValue> template2Column = createLocalizationValueTemplateField(false, false);
		TemplateField<LocalizationValue> stateColumn = createLocalizationValueTemplateField(true, true);

		keyTable.addColumn(new TableColumn<>("template1", getLocalized("translations.template1"), template1Column));
		keyTable.addColumn(new TableColumn<>("template2", getLocalized("translations.template2"), template2Column));
		keyTable.addColumn(new TableColumn<>("language", getLocalized("translations.language"), languageColumn));
		keyTable.addColumn(new TableColumn<>("status", getLocalized("translations.status"), stateColumn));


		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(selectionForm);
		verticalLayout.addComponentFillRemaining(keyTable);
		localizationKeyView.setComponent(verticalLayout);

		TemplateField<LocalizationValue> template1HeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> template2HeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> machineTranslationHeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> translationHeaderField = createLocalizationValueHeaderField(true);


		DisplayField template1ValueField = new DisplayField(true, false);
		DisplayField template2ValueField = new DisplayField(true, false);
		DisplayField machineTranslationValueField = new DisplayField(true, false);
		MultiLineTextField translationField = new MultiLineTextField();
		translationField.setCssStyle("height", "100px");

		//template1ValueField.setCssStyle("background-color", Color.RED.withAlpha(0.69f).toHtmlColorString());
		template1ValueField.setCssStyle(".field-border", "border-color", "#ec9a1a");
		template1ValueField.setCssStyle(".field-border-glow", "box-shadow", "0 0 3px 0 #ec9a1a");



		ResponsiveForm form = new ResponsiveForm(100, 120, 0);
		formLayout = form.addResponsiveFormLayout(500);
		formLayout.addSection(null, getLocalized("translations.template1")).setCollapsible(false).setDrawHeaderLine(false).setHideWhenNoVisibleFields(true);
		formLayout.addComponent(0, 0, template1HeaderField);
		formLayout.addComponent(0, 1, template1ValueField);

		formLayout.addSection(null, getLocalized("translations.template2")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);
		formLayout.addComponent(0, 0, template2HeaderField);
		formLayout.addComponent(0, 1, template2ValueField);

		formLayout.addSection(null, getLocalized("translations.automaticTranslation")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);
		formLayout.addComponent(0, 0, machineTranslationHeaderField);
		formLayout.addComponent(0, 1, machineTranslationValueField);


		formLayout.addSection(null, getLocalized("translations.translation")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);
		formLayout.addComponent(0, 0, translationHeaderField);
		formLayout.addComponent(0, 1, translationField);

		formLayout.addSection(null, getLocalized("translations.administration")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);

		translationView.setComponent(form);

		getPerspective().addView(localizationKeyView);
		getPerspective().addView(topicImageView);
		getPerspective().addView(translationView);

		previousButton.onClick.addListener(keyModelBuilder::selectPreviousRecord);
		nextButton.onClick.addListener(() -> {
			keyModelBuilder.selectNextRecord();
			keyTable.selectSingleRow(keyModelBuilder.getSelectedRecord(), false);
		});

		selectedKey.onChanged().addListener(key -> {
			Map<String, LocalizationValue> valueMap = TranslationUtils.getValueMap(key);
			LocalizationValue languageValue = valueMap.get(currentLanguage);
			LocalizationValue template1Value = valueMap.get(currentTemplate1);
			LocalizationValue template2Value = valueMap.get(currentTemplate2);
			machineTranslationHeaderField.setValue(languageValue);
			String machineTranslationValue = languageValue.getMachineTranslation();
			if (machineTranslationValue == null) {
				machineTranslationValue = " ---";
			}
			machineTranslationValueField.setValue(machineTranslationValue);
			translationHeaderField.setValue(languageValue);
			translationField.setValue(languageValue.getTranslation());
			template1HeaderField.setValue(template1Value);
			String tpl1Value = template1Value.getCurrentDisplayValue();
			if (tpl1Value == null) {
				tpl1Value = " --- ";
			}
			template1ValueField.setValue(tpl1Value);
			template2HeaderField.setValue(template2Value);
			String tpl2Value = template2Value.getCurrentDisplayValue();
			if (tpl2Value == null) {
				tpl2Value = " --- ";
			}
			template2ValueField.setValue(tpl2Value);
		});

		languageCombo.onValueChanged.addListener(language -> {
			currentLanguage = language.getIsoCode();
			Predicate<LocalizationKey> filterPredicate = TranslationUtils.getFilterPredicate(workStateComboBox.getValue(), currentLanguage);
			keyModelBuilder.setCustomFilter(filterPredicate);
		});

		template1Combo.onValueChanged.addListener(language -> {
			currentTemplate1 = language.getIsoCode();
			keyModelBuilder.updateModels();
		});

		template2Combo.onValueChanged.addListener(language -> {
			currentTemplate2 = language.getIsoCode();
			keyModelBuilder.updateModels();
		});

		workStateComboBox.onValueChanged.addListener(state -> {
			Predicate<LocalizationKey> filterPredicate = TranslationUtils.getFilterPredicate(state, currentLanguage);
			keyModelBuilder.setCustomFilter(filterPredicate);
		});


	}

	private TemplateField<LocalizationValue> createLocalizationValueHeaderField(boolean skipState) {
		TemplateField<LocalizationValue> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyProvider((value, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			Language language = Language.getLanguageByIsoCode(value.getLanguage());
			map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
			String title = language.getLanguageLocalized(getApplicationInstanceData());
			if (!skipState) {
				title += " (" + getLocalizationStateText(value) + ")";
			}
			map.put(BaseTemplate.PROPERTY_CAPTION, title);
			return map;
		});
		return templateField;
	}

	private TemplateField<LocalizationValue> createLocalizationValueTemplateField(boolean withStateIcon, boolean withStateText) {
		TemplateField<LocalizationValue> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyProvider((value, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			if (withStateIcon) {
				map.put(BaseTemplate.PROPERTY_ICON, getLocalizationStateIcon(value));
			}
			if (withStateText) {
				map.put(BaseTemplate.PROPERTY_CAPTION, getLocalizationStateText(value));
			} else {
				map.put(BaseTemplate.PROPERTY_CAPTION, value.getCurrentDisplayValue());
			}
			return map;
		});
		return templateField;
	}

	private Icon getLocalizationStateIcon(LocalizationValue value) {
		return switch (value.getTranslationVerificationState()) {
			case VERIFICATION_REQUESTED -> ApplicationIcons.HOURGLASS;
			case OK -> ApplicationIcons.OK;
			case CORRECTIONS_REQUIRED -> ApplicationIcons.SIGN_WARNING;
			default -> switch (value.getTranslationState()) {
				case TRANSLATION_REQUESTED -> ApplicationIcons.BRIEFCASE;
				case UNCLEAR -> ApplicationIcons.QUESTION;
				case NOT_NECESSARY -> ApplicationIcons.INBOX_EMPTY;
				default -> ApplicationIcons.FOLDER;
			};
		};
	}

	private String getLocalizationStateText(LocalizationValue value) {
		return switch (value.getTranslationVerificationState()) {
			case VERIFICATION_REQUESTED -> getLocalized(TranslationWorkState.VERIFICATION_REQUIRED.getTranslationKey());
			case OK -> getLocalized(TranslationWorkState.VERIFIED.getTranslationKey());
			case CORRECTIONS_REQUIRED -> getLocalized(TranslationWorkState.CORRECTIONS_REQUIRED.getTranslationKey());
			default -> switch (value.getTranslationState()) {
				case TRANSLATION_REQUESTED -> getLocalized(TranslationWorkState.TRANSLATION_REQUIRED.getTranslationKey());
				case UNCLEAR -> getLocalized(TranslationWorkState.UNCLEAR.getTranslationKey());
				case NOT_NECESSARY -> getLocalized(TranslationWorkState.TRANSLATION_NOT_NECESSARY.getTranslationKey());
				default -> "?";
			};
		};
	}

	private ComboBox<TranslationWorkState> createWorkStateComboBox() {
		PropertyExtractor<TranslationWorkState> propertyExtractor = (workState, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> workState.getIcon();
			case BaseTemplate.PROPERTY_CAPTION -> getLocalized(workState.getTranslationKey());
			default -> null;
		};
		ComboBox<TranslationWorkState> comboBox = ComboBoxUtils.createRecordComboBox(Arrays.asList(TranslationWorkState.values()), propertyExtractor, BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		return comboBox;
	}

	private ComboBox<LocalizationTopic> createTopicComboBox() {
		PropertyProvider<LocalizationTopic> propertyProvider = (PropertyExtractor<LocalizationTopic>) (localizationTopic, propertyName) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.TAGS);
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationTopic.getTitle());
			return map;
		};
		ComboBox<LocalizationTopic> comboBox = ComboBoxUtils.createRecordComboBox(LocalizationTopic.getAll(), propertyProvider, BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		return comboBox;
	}

	private ComboBox<TranslationMode> createTranslationModeComboBox() {
		PropertyExtractor<TranslationMode> propertyExtractor = (mode, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> mode.getIcon();
			case BaseTemplate.PROPERTY_CAPTION -> getLocalized(mode.getTranslationKey());
			default -> null;
		};
		ComboBox<TranslationMode> comboBox = ComboBoxUtils.createRecordComboBox(Arrays.asList(TranslationMode.values()), propertyExtractor, BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		return comboBox;
	}


	private Set<TranslationMode> getAvailableModes() {
		//todo
		return new HashSet<>(Arrays.asList(TranslationMode.values()));
	}


}
