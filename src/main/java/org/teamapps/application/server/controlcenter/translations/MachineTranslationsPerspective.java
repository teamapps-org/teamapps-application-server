package org.teamapps.application.server.controlcenter.translations;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.combo.ComboBoxUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
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
import org.teamapps.ux.component.field.FieldMessage;
import org.teamapps.ux.component.field.MultiLineTextField;
import org.teamapps.ux.component.field.TemplateField;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MachineTranslationsPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<LocalizationKey> selectedKey = TwoWayBindableValue.create();

	private String currentLanguage;
	private String currentTemplate1;
	private String currentTemplate2;

	private boolean language1Visible;
	private boolean language2Visible;
	private boolean machineTranslationVisible;


	public MachineTranslationsPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View localizationKeyView = View.createView(StandardLayout.CENTER, ApplicationIcons.EARTH_LINK, getLocalized("machineTranslation.title"), null);
		View translationView = View.createView(StandardLayout.RIGHT, ApplicationIcons.EARTH_LINK, getLocalized("machineTranslation.title"), null);
		localizationKeyView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));
		translationView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));


		ToolbarButtonGroup buttonGroup = localizationKeyView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton overViewButtonOn = buttonGroup.addButton(ToolbarButton.create(CompositeIcon.of(ApplicationIcons.SPELL_CHECK, ApplicationIcons.CHECKBOX), getLocalized("translations.overView"), getLocalized("translations.showOverView")));
		ToolbarButton overViewButtonOff = buttonGroup.addButton(ToolbarButton.create(CompositeIcon.of(ApplicationIcons.SPELL_CHECK, ApplicationIcons.DELETE), getLocalized("translations.overView"), getLocalized("translations.hideOverView")));
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


		ComboBox<Language> languageCombo = Language.createComboBox(getApplicationInstanceData());


		EntityModelBuilder<LocalizationKey> keyModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? LocalizationKey.filter().application(NumericFilter.equalsFilter(getMainApplication().getId())) : LocalizationKey.filter(), getApplicationInstanceData());
		keyModelBuilder.updateModels();
		keyModelBuilder.attachSearchField(localizationKeyView);
		keyModelBuilder.attachViewCountHandler(localizationKeyView, () -> getLocalized("translations.overView"));
		keyModelBuilder.onSelectedRecordChanged.addListener(key -> selectedKey.set(key));
		Table<LocalizationKey> keyTable = keyModelBuilder.createTable();
		keyTable.setDisplayAsList(true);
		keyTable.setStripedRows(false);

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

		TemplateField<LocalizationValue> template1Column = createLocalizationValueTemplateField(true, false);
		TemplateField<LocalizationValue> template2Column = createLocalizationValueTemplateField(true, false);
		TemplateField<LocalizationValue> languageColumn = createLocalizationValueTemplateField(false, false);
		TemplateField<LocalizationValue> stateColumn = createLocalizationValueTemplateField(true, true);

		keyTable.addColumn(new TableColumn<>("template1", getLocalized("translations.template1"), template1Column));
		keyTable.addColumn(new TableColumn<>("template2", getLocalized("translations.template2"), template2Column));
		keyTable.addColumn(new TableColumn<>("language", getLocalized("translations.language"), languageColumn));
		keyTable.addColumn(new TableColumn<>("status", getLocalized("translations.status"), stateColumn));

		Function<String, String> languageByTableFieldNameFunction = field -> switch (field) {
			case "template1" -> currentTemplate1;
			case "template2" -> currentTemplate2;
			case "language" -> currentLanguage;
			default -> null;
		};
		keyModelBuilder.setCustomFieldSorter(fieldName -> {
			String language = languageByTableFieldNameFunction.apply(fieldName);
			if (language != null) {
				return (k1, k2) -> TranslationUtils.getDisplayValueNonNull(k1, language).compareToIgnoreCase(TranslationUtils.getDisplayValueNonNull(k2, language));
			}
			return null; //todo sort by state
		});

		localizationKeyView.setComponent(keyTable);

		TemplateField<LocalizationValue> template1HeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> template2HeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> machineTranslationHeaderField = createLocalizationValueHeaderField(true);
		TemplateField<LocalizationValue> translationHeaderField = createLocalizationValueHeaderField(true);


		DisplayField template1ValueField = new DisplayField(true, false);
		DisplayField template2ValueField = new DisplayField(true, false);
		DisplayField machineTranslationValueField = new DisplayField(true, false);
		MultiLineTextField translationField = new MultiLineTextField();
		translationField.setCssStyle("height", "100px");

		MultiLineTextField proofReadNotesField = new MultiLineTextField();
		proofReadNotesField.setCssStyle("height", "100px");


		//template1ValueField.setCssStyle("background-color", Color.RED.withAlpha(0.69f).toHtmlColorString());
		template1ValueField.setCssStyle(".field-border", "border-color", "#ec9a1a");
		template1ValueField.setCssStyle(".field-border-glow", "box-shadow", "0 0 3px 0 #ec9a1a");


		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(500);
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

		formLayout.addSection(null, getLocalized("translations.mode.proofread")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);
		formLayout.addLabelAndField(null, getLocalized("translations.errorNotes"), proofReadNotesField);

		formLayout.addSection(null, getLocalized("translations.administration")).setCollapsible(false).setDrawHeaderLine(true).setHideWhenNoVisibleFields(true);

		translationView.setComponent(form);

//		getPerspective().addView(localizationKeyView);
//		getPerspective().addView(topicImageView);
//		getPerspective().addView(translationView);
//
//		Consumer<TranslationMode> translationModeChangeHandler = translationMode -> {
//			if (translationMode == null) translationMode = getAvailableModes().get(0);
//			switch (translationMode) {
//				case TRANSLATE -> {
//					doneButton.setVisible(true);
//					unclearButton.setVisible(true);
//					copyTranslationButton.setVisible(true);
//					verifiedButton.setVisible(false);
//					incorrectButton.setVisible(false);
//					proofReadNotesField.setVisible(false);
//					workStateComboBox.setValue(TranslationWorkState.TRANSLATION_REQUIRED);
//				}
//				case PROOFREAD -> {
//					doneButton.setVisible(false);
//					unclearButton.setVisible(false);
//					copyTranslationButton.setVisible(false);
//					verifiedButton.setVisible(true);
//					incorrectButton.setVisible(true);
//					proofReadNotesField.setVisible(true);
//					workStateComboBox.setValue(TranslationWorkState.VERIFICATION_REQUIRED);
//				}
//				case ADMINISTRATE -> {
//				}
//			}
//			Predicate<LocalizationKey> filterPredicate = TranslationUtils.getFilterPredicate(workStateComboBox.getValue(), currentLanguage, topicComboBox.getValue());
//			keyModelBuilder.setCustomFilter(filterPredicate);
//		};
//		translationModeChangeHandler.accept(getAvailableModes().get(0));
//
//		modeComboBox.onValueChanged.addListener(translationModeChangeHandler);
//
//		previousButton.onClick.addListener(keyModelBuilder::selectPreviousRecord);
//		nextButton.onClick.addListener(keyModelBuilder::selectNextRecord);
//
//		doneButton.onClick.addListener(() -> {
//			LocalizationValue value = TranslationUtils.getValue(selectedKey.get(), currentLanguage);
//			LocalizationValue templateValue = TranslationUtils.getValue(selectedKey.get(), currentTemplate1);
//			String translation = translationField.getValue();
//			if (translation != null && value != null && templateValue != null && templateValue.getCurrentDisplayValue() != null &&
//					(TranslationUtils.createTranslationStates(TranslationState.TRANSLATION_REQUESTED, TranslationState.UNCLEAR)).contains(value.getTranslationState())) {
//				if (translation.contains("\n") && !templateValue.getCurrentDisplayValue().contains("\n")) {
//					UiUtils.showNotification(ApplicationIcons.ERROR, getLocalized("translations.translationMayNotContainLineBreaks"));
//					return;
//				}
//				value
//						.setTranslation(translation)
//						.setTranslationState(TranslationState.OK)
//						.setTranslationVerificationState(TranslationVerificationState.VERIFICATION_REQUESTED)
//						.save();
//				keyModelBuilder.selectNextRecord();
//				UiUtils.showSaveNotification(true, getApplicationInstanceData());
//			}
//		});
//
//		unclearButton.onClick.addListener(() -> {
//			LocalizationValue value = TranslationUtils.getValue(selectedKey.get(), currentLanguage);
//			if (value != null && value.getTranslationState() == TranslationState.TRANSLATION_REQUESTED) {
//				value.setTranslationState(TranslationState.UNCLEAR).save();
//				keyModelBuilder.selectNextRecord();
//				keyModelBuilder.updateModels();
//				UiUtils.showNotification(ApplicationIcons.OK, getLocalized("translations.translationSuccessfullyRejected"));
//			}
//		});
//
//		copyTranslationButton.onClick.addListener(() -> {
//			LocalizationValue value = TranslationUtils.getValue(selectedKey.get(), currentLanguage);
//			translationField.setValue(value.getMachineTranslation());
//		});
//
//		verifiedButton.onClick.addListener(() -> {
//			LocalizationValue value = TranslationUtils.getValue(selectedKey.get(), currentLanguage);
//			if (value != null && value.getTranslation() != null && value.getTranslationState() == TranslationState.OK) {
//				value.setTranslationVerificationState(TranslationVerificationState.OK).save();
//				keyModelBuilder.selectNextRecord();
//				UiUtils.showSaveNotification(true, getApplicationInstanceData());
//			}
//		});
//
//		incorrectButton.onClick.addListener(() -> {
//			LocalizationValue value = TranslationUtils.getValue(selectedKey.get(), currentLanguage);
//			String notes = proofReadNotesField.getValue();
//			if (notes != null && value != null && value.getTranslation() != null && value.getTranslationState() == TranslationState.OK) {
//				value
//						.setTranslationVerificationState(TranslationVerificationState.CORRECTIONS_REQUIRED)
//						.setTranslationState(TranslationState.TRANSLATION_REQUESTED)
//						.setNotes(notes)
//						.save();
//				keyModelBuilder.selectNextRecord();
//				UiUtils.showNotification(ApplicationIcons.OK, getLocalized("translations.translationSuccessfullyRejected"));
//			}
//		});
//
//
//		selectedKey.onChanged().addListener(key -> {
//			Map<String, LocalizationValue> valueMap = TranslationUtils.getValueMap(key);
//			translationField.clearCustomFieldMessages();
//			LocalizationValue languageValue = valueMap.get(currentLanguage);
//			LocalizationValue template1Value = valueMap.get(currentTemplate1);
//			LocalizationValue template2Value = valueMap.get(currentTemplate2);
//			machineTranslationHeaderField.setValue(languageValue);
//			machineTranslationValueField.setValue(languageValue == null ? " --- " : languageValue.getMachineTranslation() != null ? languageValue.getMachineTranslation() : " --- ");
//			translationHeaderField.setValue(languageValue);
//			translationField.setValue(languageValue != null ? languageValue.getTranslation() : null);
//			template1HeaderField.setValue(template1Value);
//			template1ValueField.setValue(template1Value == null ? " --- " : template1Value.getCurrentDisplayValue() != null ? template1Value.getCurrentDisplayValue() : " --- ");
//			template2HeaderField.setValue(template2Value);
//			template2ValueField.setValue(template2Value == null ? " --- " : template2Value.getCurrentDisplayValue() != null ? template2Value.getCurrentDisplayValue() : " --- ");
//			if (languageValue != null && languageValue.getNotes() != null) {
//				translationField.addCustomFieldMessage(FieldMessage.Severity.WARNING, languageValue.getNotes());
//			}
//			proofReadNotesField.setValue(languageValue != null ? languageValue.getNotes() : null);
//		});
//
//		languageCombo.onValueChanged.addListener(language -> {
//			currentLanguage = language != null ? language.getIsoCode() : null;
//			Predicate<LocalizationKey> filterPredicate = TranslationUtils.getFilterPredicate(workStateComboBox.getValue(), currentLanguage, topicComboBox.getValue());
//			keyModelBuilder.setCustomFilter(filterPredicate);
//		});
//
//		template1Combo.onValueChanged.addListener(language -> {
//			currentTemplate1 = language != null ? language.getIsoCode() : null;
//			keyModelBuilder.updateModels();
//		});
//
//		template2Combo.onValueChanged.addListener(language -> {
//			currentTemplate2 = language != null ? language.getIsoCode() : null;
//			keyModelBuilder.updateModels();
//		});
//
//		workStateComboBox.onValueChanged.addListener(state -> {
//			Predicate<LocalizationKey> filterPredicate = state != null ? TranslationUtils.getFilterPredicate(state, currentLanguage, topicComboBox.getValue()) : null;
//			keyModelBuilder.setCustomFilter(filterPredicate);
//		});
//
//		topicComboBox.onValueChanged.addListener(topic -> {
//			Predicate<LocalizationKey> filterPredicate = TranslationUtils.getFilterPredicate(workStateComboBox.getValue(), currentLanguage, topic);
//			keyModelBuilder.setCustomFilter(filterPredicate);
//		});


	}

	private TemplateField<LocalizationValue> createLocalizationValueHeaderField(boolean skipState) {
		TemplateField<LocalizationValue> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyProvider((value, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			if (value == null) return map;
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
			if (value == null) return map;
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
		if (value == null) return null;
		return switch (value.getTranslationVerificationState()) {
			case VERIFICATION_REQUESTED -> ApplicationIcons.CHECKS;
			case OK -> ApplicationIcons.OK;
			case CORRECTIONS_REQUIRED -> ApplicationIcons.SIGN_WARNING;
			default -> switch (value.getTranslationState()) {
				case TRANSLATION_REQUESTED -> ApplicationIcons.BRIEFCASE;
				case UNCLEAR -> ApplicationIcons.QUESTION;
				case NOT_NECESSARY -> ApplicationIcons.OK;
				default -> ApplicationIcons.FOLDER;
			};
		};
	}

	private String getLocalizationStateText(LocalizationValue value) {
		if (value == null) return null;
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
		PropertyProvider<Application> applicationPropertyProvider = PropertyProviders.createApplicationPropertyProvider(userSessionData);
		PropertyProvider<LocalizationTopic> propertyProvider = (localizationTopic, propertyName) -> {
			Map<String, Object> map = new HashMap<>();
			if (localizationTopic == null) return map;
			if (localizationTopic.getApplication() != null) {
				Map<String, Object> values = applicationPropertyProvider.getValues(localizationTopic.getApplication(), Collections.emptyList());
				map.put(BaseTemplate.PROPERTY_ICON, values.get(BaseTemplate.PROPERTY_ICON));
				map.put(BaseTemplate.PROPERTY_CAPTION, values.get(BaseTemplate.PROPERTY_CAPTION));
			} else {
				map.put(BaseTemplate.PROPERTY_ICON, localizationTopic.getIcon() != null ? IconUtils.decodeIcon(localizationTopic.getIcon()) : ApplicationIcons.TAGS);
				map.put(BaseTemplate.PROPERTY_CAPTION, localizationTopic.getTitle());
			}
			return map;
		};
		ComboBox<LocalizationTopic> comboBox = ComboBoxUtils.createRecordComboBox(isAppFilter() ? LocalizationTopic.filter().application(NumericFilter.equalsFilter(getMainApplication().getId())).execute() : LocalizationTopic.getAll(), propertyProvider, BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		comboBox.setShowClearButton(true);
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


	private List<TranslationMode> getAvailableModes() {
		//todo
		return Arrays.asList(TranslationMode.values());
	}


}
