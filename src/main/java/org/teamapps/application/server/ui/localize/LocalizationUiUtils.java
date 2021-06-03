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
package org.teamapps.application.server.ui.localize;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.LocalizationKey;
import org.teamapps.model.controlcenter.LocalizationKeyType;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.index.text.TextFilter;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LocalizationUiUtils {

	public static ComboBox<String> createLocalizationKeyCombo(Template template, ApplicationLocalizationProvider localizationProvider) {
		return createLocalizationKeyCombo(template, localizationProvider, null);
	}

	public static ComboBox<String> createLocalizationKeyCombo(Template template, ApplicationLocalizationProvider localizationProvider, Supplier<Application> applicationSupplier) {
		ComboBox<String> comboBox = new ComboBox<>(template);
		comboBox.setPropertyProvider((s, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			if (s == null || s.isBlank()) {
				return map;
			}
			LocalizationKey localizationKey = LocalizationKey.filter().key(TextFilter.textEqualsFilter(s)).executeExpectSingleton();
			if (localizationKey == null) {
				map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.SYMBOL_QUESTIONMARK);
				map.put(BaseTemplate.PROPERTY_CAPTION, s);
				return map;
			}
			map.put(BaseTemplate.PROPERTY_ICON, getLocalizationKeyIcon(localizationKey));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(localizationKey.getKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationKey.getKey());
			return map;
		});
		comboBox.setRecordToStringFunction(localizationProvider::getLocalized);
		comboBox.setModel(query -> {
			List<LocalizationKey> keys = new ArrayList<>();
			keys.addAll(applicationSupplier != null && applicationSupplier.get() != null ? LocalizationKey.filter().application(NumericFilter.equalsFilter(applicationSupplier.get().getId())).execute() : Collections.emptyList());
			keys.addAll(LocalizationKey.filter().localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.DICTIONARY_KEY).execute());
			keys.addAll(LocalizationKey.filter().localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.SYSTEM_KEY).execute());
			if (query == null || query.isBlank()) {
				return keys.stream()
						.limit(150)
						.map(LocalizationKey::getKey)
						.collect(Collectors.toList());
			} else {
				String q = query.toLowerCase();
				return keys.stream()
						.filter(key -> key.getKey().toLowerCase().contains(q) || localizationProvider.getLocalized(key.getKey()).toLowerCase().contains(q))
						.limit(150)
						.map(LocalizationKey::getKey)
						.collect(Collectors.toList());
			}
		});
		return comboBox;
	}

	public static Icon getLocalizationKeyIcon(LocalizationKey key) {
		if (key == null) {
			return null;
		}
		return switch (key.getLocalizationKeyType()) {
			case APPLICATION_RESOURCE_KEY -> IconUtils.decodeIcon(key.getApplication().getIcon());
			case DICTIONARY_KEY -> ApplicationIcons.DICTIONARY;
			case REPORTING_KEY -> ApplicationIcons.DOCUMENT_NOTEBOOK;
			case SYSTEM_KEY -> ApplicationIcons.SYSTEM;
		};
	}
}
