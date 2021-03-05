package org.teamapps.application.server.system.localization;

import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.LocalizationKey;
import org.teamapps.model.controlcenter.LocalizationValue;
import org.teamapps.universaldb.index.numeric.NumericFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationScopeLocalizationProvider implements LocalizationProvider {

	private final Application application;
	private Map<String, Map<String, LocalizationValue>> localizationLanguageValueMapByKey;

	public ApplicationScopeLocalizationProvider(Application application) {
		this.application = application;
		loadLocalizationData();
	}

	private void loadLocalizationData() {
		try {
			localizationLanguageValueMapByKey = new HashMap<>();
			LocalizationKey.filter()
					.application(NumericFilter.equalsFilter(application.getId()))
					.execute()
					.stream()
					.flatMap(key -> key.getLocalizationValues().stream()).forEach(value -> {
				localizationLanguageValueMapByKey.computeIfAbsent(value.getLocalizationKey().getKey(), k -> new HashMap<>()).put(value.getLanguage(), value);
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public String getLocalizationValue(String key, List<String> languagePriorityOrder) {
		if (key == null || key.isEmpty()) {
			return key;
		}
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
