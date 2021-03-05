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
import java.util.concurrent.ConcurrentHashMap;

public class SystemLocalizationProvider implements LocalizationProvider {

	public final static String SYSTEM_KEY_PREFIX = "org.teamapps.systemKey.";

	private Map<String, Map<String, LocalizationValue>> localizationLanguageValueMapByKey;

	public SystemLocalizationProvider(TranslationService translationService, List<String> requiredLanguages) {
		loadData();
	}

	private void loadData() {
		localizationLanguageValueMapByKey = new ConcurrentHashMap<>();
		LocalizationKey.filter()
				.application(NumericFilter.equalsFilter(0))
				.localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.DICTIONARY_KEY)
				.execute()
				.stream()
				.flatMap(key -> key.getLocalizationValues().stream()).forEach(value -> {
			localizationLanguageValueMapByKey.computeIfAbsent(value.getLocalizationKey().getKey(), k -> new HashMap<>()).put(value.getLanguage(), value);
		});
	}

	public void addKey(String key, String language, String value) {
		if (!key.startsWith(SYSTEM_KEY_PREFIX)) {
			key = SYSTEM_KEY_PREFIX + key;
		}
		LocalizationKey localizationKey = LocalizationKey.create()
				.setKey(key)
				.setLocalizationKeyType(LocalizationKeyType.SYSTEM_KEY)
				.setUsed(true)
				.save();
		LocalizationValue localizationValue = LocalizationValue.create()
				.setLocalizationKey(localizationKey)
				.setLanguage(language)
				.setOriginal(value)
				.save();
		localizationLanguageValueMapByKey.computeIfAbsent(localizationKey.getKey(), k -> new HashMap<>()).put(localizationValue.getLanguage(), localizationValue);
		//todo create translation values
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
