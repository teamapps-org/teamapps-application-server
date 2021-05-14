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
package org.teamapps.application.server.system.localization;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.LocalizationKey;
import org.teamapps.model.controlcenter.LocalizationValue;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.session.SessionContext;

import java.text.MessageFormat;
import java.util.*;

public class SessionLocalizationProvider implements ApplicationLocalizationProvider {

	private final List<String> rankedLanguages;
	private final DictionaryLocalizationProvider dictionary;
	private final SystemLocalizationProvider systemDictionary;
	private Map<String, Map<String, LocalizationValue>> allKeysMap;
	private Map<Application, Map<String, Map<String, LocalizationValue>>> applicationLocalizationMap;

	public SessionLocalizationProvider(SystemRegistry registry, SessionContext context) {
		this(Arrays.asList(context.getLocale().getLanguage()), registry.getDictionary(), registry.getSystemDictionary());
	}

	public SessionLocalizationProvider(List<String> rankedLanguages, DictionaryLocalizationProvider dictionary, SystemLocalizationProvider systemDictionary) {
		this.rankedLanguages = new ArrayList<>(rankedLanguages);
		this.dictionary = dictionary;
		this.systemDictionary = systemDictionary;
		if (!this.rankedLanguages.contains("en")) {
			this.rankedLanguages.add("en");
		}
		loadLocalizationData();
	}

	public SessionApplicationLocalizationProvider createApplicationLocalizationProvider(Application application) {
		return new SessionApplicationLocalizationProvider(application, this);
	}

	private void loadLocalizationData() {
		try {
			allKeysMap = new HashMap<>();
			applicationLocalizationMap = new HashMap<>();
			LocalizationKey.filter()
					.execute()
					.stream()
					.flatMap(key -> key.getLocalizationValues().stream()).forEach(value -> {
				allKeysMap.computeIfAbsent(value.getLocalizationKey().getKey(), k -> new HashMap<>()).put(value.getLanguage(), value);
				applicationLocalizationMap
						.computeIfAbsent(value.getLocalizationKey().getApplication(), k -> new HashMap<>())
						.computeIfAbsent(value.getLocalizationKey().getKey(), k -> new HashMap<>()).put(value.getLanguage(), value);
			});

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public String getLocalized(Application application, String key) {
		if (key == null || key.isEmpty()) {
			return key;
		}
		if (key.startsWith(LocalizationData.DICTIONARY_PREFIX)) {
			return dictionary.getLocalizationValue(key, rankedLanguages);
		} else if (key.startsWith(SystemLocalizationProvider.SYSTEM_KEY_PREFIX)) {
			return systemDictionary.getLocalizationValue(key, rankedLanguages);
		} else {
			String result = getLocalizationValue(key, applicationLocalizationMap.get(application));
			if (result == null) {
				result = getLocalizationValue(key, allKeysMap);
			}
			return result != null ? result : key;
		}
	}


	public String getLocalized(Application application, String key, Object... parameters) {
		String localizationValue = getLocalized(application, key);
		if (parameters != null && parameters.length > 0) {
			try {
				return MessageFormat.format(localizationValue, parameters);
			} catch (Exception e) {
				e.printStackTrace();
				return localizationValue;
			}
		} else {
			return localizationValue;
		}
	}

	@Override
	public String getLocalized(String key, Object... parameters) {
		return getLocalized(null, key, parameters);
	}

	@Override
	public String getLocalized(TranslatableText translatableText) {
		if (translatableText == null) {
			return null;
		} else {
			Map<String, String> translationMap = translatableText.getTranslationMap();
			for (String language : rankedLanguages) {
				String value = translationMap.get(language);
				if (value != null) {
					return value;
				}
			}
			return translatableText.getText();
		}
	}

	private String getLocalizationValue(String key, Map<String, Map<String, LocalizationValue>> localizationMap) {
		if (localizationMap == null) {
			return null;
		}
		Map<String, LocalizationValue> languageValueMap = localizationMap.get(key);
		if (languageValueMap != null) {
			for (String language : rankedLanguages) {
				LocalizationValue value = languageValueMap.get(language);
				if (value != null && value.getCurrentDisplayValue() != null) {
					return value.getCurrentDisplayValue();
				}
			}
		}
		return null;
	}
}