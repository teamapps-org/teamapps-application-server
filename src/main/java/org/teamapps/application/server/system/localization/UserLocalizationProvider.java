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
import org.teamapps.universaldb.index.translation.TranslatableText;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserLocalizationProvider implements ApplicationLocalizationProvider, LocalizationProvider {

	private final List<String> rankedLanguages;
	private final DictionaryLocalizationProvider dictionary;
	private final SystemLocalizationProvider systemDictionary;
	private final ApplicationScopeLocalizationProvider applicationScopeLocalizationProvider;

	public UserLocalizationProvider(List<String> rankedLanguages, DictionaryLocalizationProvider dictionary, SystemLocalizationProvider systemDictionary) {
		this(rankedLanguages, dictionary, systemDictionary, null);
	}

	public UserLocalizationProvider(List<String> rankedLanguages, DictionaryLocalizationProvider dictionary, SystemLocalizationProvider systemDictionary, ApplicationScopeLocalizationProvider applicationScopeLocalizationProvider) {
		this.rankedLanguages = new ArrayList<>(rankedLanguages);
		this.dictionary = dictionary;
		this.systemDictionary = systemDictionary;
		this.applicationScopeLocalizationProvider = applicationScopeLocalizationProvider;
		if (!this.rankedLanguages.contains("en")) {
			this.rankedLanguages.add("en");
		}
	}

	@Override
	public String getLocalizationValue(String key, List<String> languagePriorityOrder) {
		if (key == null || key.isEmpty()) {
			return key;
		}
		if (key.startsWith(LocalizationData.DICTIONARY_PREFIX)) {
			return dictionary.getLocalizationValue(key, languagePriorityOrder);
		} else if (key.startsWith(SystemLocalizationProvider.SYSTEM_KEY_PREFIX)) {
			return systemDictionary.getLocalizationValue(key, languagePriorityOrder);
		} else if (applicationScopeLocalizationProvider != null) {
			return applicationScopeLocalizationProvider.getLocalizationValue(key, languagePriorityOrder);
		}
		return null;
	}

	@Override
	public String getLocalized(String key, Object... parameters) {
		String localizationValue = getLocalizationValue(key, rankedLanguages);
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
}
