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
