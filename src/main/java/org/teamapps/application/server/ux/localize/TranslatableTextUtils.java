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
package org.teamapps.application.server.ux.localize;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TranslatableTextUtils {

	public static Function<TranslatableText, String> createTranslatableTextExtractor(ApplicationInstanceData applicationInstanceData) {
		List<String> rankedLanguages = applicationInstanceData.getUser().getRankedLanguages();
		return createTranslatableTextExtractor(rankedLanguages);
	}

	public static Function<TranslatableText, String> createTranslatableTextExtractor(List<String> rankedLanguages) {
		return translatableText -> {
			if (translatableText == null) {
				return null;
			}
			Map<String, String> translationMap = translatableText.getTranslationMap();
			for (String language : rankedLanguages) {
				String value = translationMap.get(language);
				if (value != null) {
					return value;
				}
			}
			return translatableText.getText();
		};
	}

	public static TranslatableField createTranslatableField(ApplicationInstanceData applicationInstanceData) {
		return new TranslatableField(applicationInstanceData);
	}

	public static TemplateField<TranslatableText> createTranslatableTemplateField(ApplicationInstanceData applicationInstanceData) {
		TemplateField<TranslatableText> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyProvider(createPropertyProvider(applicationInstanceData));
		return templateField;
	}


	public static PropertyProvider<TranslatableText> createPropertyProvider(ApplicationInstanceData applicationInstanceData) {
		List<String> rankedLanguages = applicationInstanceData.getUser().getRankedLanguages();
		return (translatableText, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			Map<String, String> translationMap = translatableText.getTranslationMap();
			for (String languageIso : rankedLanguages) {
				String text = translationMap.get(languageIso);
				if (text != null) {
					Language language = Language.getLanguageByIsoCode(languageIso);
					if (language != null) {
						map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
						map.put(BaseTemplate.PROPERTY_CAPTION, text);
						map.put(BaseTemplate.PROPERTY_DESCRIPTION, language.getIsoCode() + " (" + language.getLanguageLocalized(applicationInstanceData) + ")");
						return map;
					}
				}
			}
			if (translatableText.getText() != null) {
				Language language = Language.getLanguageByIsoCode(translatableText.getOriginalLanguage());
				if (language != null) {
					map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
					map.put(BaseTemplate.PROPERTY_CAPTION, translatableText.getText());
					map.put(BaseTemplate.PROPERTY_DESCRIPTION, language.getIsoCode() + " (" + language.getLanguageLocalized(applicationInstanceData) + ")");
					return map;
				} else {
					map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.SYMBOL_QUESTIONMARK);
					map.put(BaseTemplate.PROPERTY_CAPTION, translatableText.getText());
					map.put(BaseTemplate.PROPERTY_DESCRIPTION, translatableText.getOriginalLanguage());
				}
			}
			return map;
		};
	}
}
