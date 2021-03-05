package org.teamapps.application.server.system.localization;

import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.model.controlcenter.LocalizationKey;
import org.teamapps.model.controlcenter.LocalizationKeyType;
import org.teamapps.model.controlcenter.LocalizationValue;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.numeric.NumericFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryLocalizationProvider implements LocalizationProvider {

	private Map<String, Map<String, LocalizationValue>> localizationLanguageValueMapByKey;

	public DictionaryLocalizationProvider(TranslationService translationService, List<String> requiredLanguages) {
		synchronizeDictionaryData(requiredLanguages);
		loadDictionary();
		translateDictionary(translationService);
	}

	private void synchronizeDictionaryData(List<String> requiredLanguages) {
		LocalizationUtil.synchronizeLocalizationData(LocalizationData.createDictionaryData(getClass().getClassLoader()), null, LocalizationKeyType.DICTIONARY_KEY, requiredLanguages);
	}

	private void loadDictionary() {
		localizationLanguageValueMapByKey = new HashMap<>();
		LocalizationKey.filter()
				.application(NumericFilter.equalsFilter(0))
				.localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.DICTIONARY_KEY)
				.execute()
				.stream()
				.flatMap(key -> key.getLocalizationValues().stream()).forEach(value -> {
			localizationLanguageValueMapByKey.computeIfAbsent(value.getLocalizationKey().getKey(), k -> new HashMap<>()).put(value.getLanguage(), value);
		});
	}

	private void translateDictionary(TranslationService translationService) {
		LocalizationUtil.translateAllDictionaryValues(translationService);
	}

	public String getLocalizationValue(String key, List<String> languagePriorityOrder) {
		Map<String, LocalizationValue> languageValueMap = localizationLanguageValueMapByKey.get(key);
		if (languageValueMap != null) {
			for (String language : languagePriorityOrder) {
				LocalizationValue value = languageValueMap.get(language);
				if (value != null && value.getCurrentDisplayValue() != null) {
					return value.getCurrentDisplayValue();
				}
			}
		}
		return key;
	}


}
