package org.teamapps.application.server.ux.localize;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Language;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.model.ListTreeModel;

import java.util.*;
import java.util.stream.Collectors;

public class TranslatableField extends ComboBox<TranslatableText> {

	private final ApplicationInstanceData applicationInstanceData;
	private List<Language> rankedLanguages;
	private TranslatableText originalText;
	private ListTreeModel<TranslatableText> model;
	private TranslatableFieldEntry selectedEntry;

	public TranslatableField(ApplicationInstanceData applicationInstanceData) {
		this.applicationInstanceData = applicationInstanceData;
		init();
		setValue(null);
	}

	private void init() {
		setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
		this.rankedLanguages = applicationInstanceData.getUser().getRankedLanguages().stream()
				.filter(iso -> Language.getLanguageByIsoCode(iso) != null)
				.map(Language::getLanguageByIsoCode)
				.collect(Collectors.toList());
		setPropertyProvider(createPropertyProvider());
		model = new ListTreeModel<>(Collections.emptyList(), (translatableText, s) -> false);
		setModel(model);
		setShowDropDownAfterResultsArrive(true);

		setAllowFreeText(true);
		setRecordToStringFunction(translatableText -> convert(translatableText).getValue());
		setShowDropDownAfterResultsArrive(true);

		setFreeTextRecordFactory(s -> {
			TranslatableFieldEntry entry = this.selectedEntry;
			entry.setValue(s);
			return entry;
		});

		onValueChanged.addListener(value -> {
			TranslatableFieldEntry entry = convert(value);
			selectedEntry = entry;
			super.setValue(entry); //combo box does not properly render values set via setRecordToStringFunction
		});

	}

	private PropertyProvider<TranslatableText> createPropertyProvider() {
		return (translatableText, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			TranslatableFieldEntry entry = convert(translatableText);
			map.put(BaseTemplate.PROPERTY_ICON, entry.getLanguage().getIcon());
			map.put(BaseTemplate.PROPERTY_CAPTION, entry.getValue());
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, entry.getLanguage().getIsoCode() + " - " + entry.getLanguage().getLanguageLocalized(applicationInstanceData));
			return map;
		};
	}

	private TranslatableFieldEntry convert(TranslatableText translatableText) {
		return (TranslatableFieldEntry) translatableText;
	}

	@Override
	public void setValue(TranslatableText translatableText) {
		this.originalText = translatableText;
		List<TranslatableText> modelValues = new ArrayList<>();
		if (translatableText == null) {
			for (Language language : rankedLanguages) {
				modelValues.add(new TranslatableFieldEntry(null, language, null));
			}

		} else {
			Map<String, String> translationMap = translatableText.getTranslationMap();
			Set<Language> availableLanguages = new HashSet<>();
			for (Map.Entry<String, String> mapEntry : translationMap.entrySet()) {
				Language language = Language.getLanguageByIsoCode(mapEntry.getKey());
				if (language != null) {
					availableLanguages.add(language);
					modelValues.add(new TranslatableFieldEntry(mapEntry.getValue(), language, translatableText));
				}
			}
			for (Language language : rankedLanguages) {
				if (!availableLanguages.contains(language)) {
					modelValues.add(new TranslatableFieldEntry(null, language, translatableText));
				}
			}
		}
		TranslatableText record = modelValues.get(0);
		selectedEntry = convert(record);
		model.setRecords(modelValues);
		super.setValue(record);
	}

	@Override
	public TranslatableText getValue() {
		List<TranslatableFieldEntry> entries = model.getRecords().stream()
				.map(this::convert)
				.collect(Collectors.toList());
		if (originalText != null) {
			for (TranslatableFieldEntry entry : entries) {
				originalText.setTranslation(entry.getValue(), entry.getLanguage().getIsoCode());
			}
			return originalText;
		} else {
			TranslatableText text = null;
			for (TranslatableFieldEntry entry : entries) {
				if (entry.getValue() != null) {
					if (text == null) {
						text = TranslatableText.create(entry.getValue(), entry.getLanguage().getIsoCode());
					} else {
						text.setTranslation(entry.getValue(), entry.getLanguage().getIsoCode());
					}
				}
			}
			return text;
		}
	}
}
