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

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.localization.SystemLocalizationProvider;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.form.FormWindow;
import org.teamapps.event.Event;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.index.text.TextFilter;
import org.teamapps.ux.component.field.FieldMessage;
import org.teamapps.ux.component.field.Fields;
import org.teamapps.ux.component.field.TextField;

import java.util.*;
import java.util.stream.Collectors;

public class LocalizationKeyWindow {

	public final Event<String> onNewKey = new Event<>();
	private final ApplicationInstanceData applicationInstanceData;
	private final SystemLocalizationProvider systemLocalizationProvider;
	private final List<Language> rankedLanguages;
	private final Application application;
	private FormWindow formWindow;
	private String newLocalizationKey;

	public static LocalizationKeyWindow createSystemKey(SystemLocalizationProvider systemLocalizationProvider, ApplicationInstanceData applicationInstanceData) {
		return new LocalizationKeyWindow(applicationInstanceData, null, systemLocalizationProvider);
	}

	public static LocalizationKeyWindow createApplicationKey(Application application, ApplicationInstanceData applicationInstanceData) {
		return new LocalizationKeyWindow(applicationInstanceData, application, null);
	}


	private LocalizationKeyWindow(ApplicationInstanceData applicationInstanceData, Application application, SystemLocalizationProvider systemLocalizationProvider) {
		this.applicationInstanceData = applicationInstanceData;
		this.systemLocalizationProvider = systemLocalizationProvider;
		this.rankedLanguages = applicationInstanceData.getUser().getRankedLanguages().stream()
				.map(Language::getLanguageByIsoCode)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		this.application = application;
		createUi();
	}

	private void createUi() {
		formWindow = new FormWindow(ApplicationIcons.ADDRESS_BOOK, applicationInstanceData.getLocalized(Dictionary.TRANSLATION_KEY), applicationInstanceData);
		formWindow.setWindowSize(600, 450);
		formWindow.addSaveButton();
		formWindow.addCancelButton();
		formWindow.addSection();

		TextField keyField = new TextField();
		formWindow.addField(ApplicationIcons.KEY, applicationInstanceData.getLocalized(Dictionary.TRANSLATION_KEY), keyField);
		Map<Language, TextField> valueFieldByLanguage = new HashMap<>();
		for (Language language : rankedLanguages) {
			TextField valueField = new TextField();
			valueFieldByLanguage.put(language, valueField);
			formWindow.addField(language.getIcon(), language.getLanguageLocalized(applicationInstanceData), valueField);
		}

		keyField.setRequired(true);
		keyField.addValidator(s -> {
			LocalizationKeyQuery keyQuery = LocalizationKey.filter();
			if (application != null) {
				keyQuery
						.key(TextFilter.textEqualsIgnoreCaseFilter(s))
						.application(NumericFilter.equalsFilter(application.getId()));
			} else {
				keyQuery
						.key(TextFilter.textEqualsIgnoreCaseFilter(SystemLocalizationProvider.SYSTEM_KEY_PREFIX + s))
						.localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.SYSTEM_KEY);
			}
			LocalizationKey key = keyQuery.executeExpectSingleton();
			if (key != null || s == null || s.length() < 3 || s.contains(" ")) {
				return Collections.singletonList(new FieldMessage(FieldMessage.Severity.ERROR, applicationInstanceData.getLocalized(Dictionary.ERROR_TRANSLATION_KEY_ALREADY_EXISTS)));
			} else {
				return null;
			}
		});

		formWindow.getSaveButton().onClick.addListener(() -> {
			boolean success = false;
			if (Fields.validateAll(keyField)) {
				LocalizationKey localizationKey = LocalizationKey.create()
						.setKey(keyField.getValue())
						.setUsed(true)
						.setLocalizationKeyFormat(LocalizationKeyFormat.SINGLE_LINE);
				if (application != null) {
					localizationKey
							.setApplication(application)
							.setLocalizationKeyType(LocalizationKeyType.REPORTING_KEY);
				} else {
					localizationKey
							.setKey(SystemLocalizationProvider.SYSTEM_KEY_PREFIX + keyField.getValue())
							.setLocalizationKeyType(LocalizationKeyType.SYSTEM_KEY);
				}
				List<LocalizationValue> values = new ArrayList<>();
				for (Language language : rankedLanguages) {
					TextField field = valueFieldByLanguage.get(language);
					String value = field.getValue();
					if (value != null && !value.isBlank()) {
						LocalizationValue localizationValue = LocalizationValue.create()
								.setLanguage(language.getIsoCode())
								.setOriginal(value)
								.setCurrentDisplayValue(value)
								.setMachineTranslationState(MachineTranslationState.NOT_NECESSARY)
								.setTranslationState(TranslationState.NOT_NECESSARY)
								.setTranslationVerificationState(TranslationVerificationState.NOT_NECESSARY);
						values.add(localizationValue);
					}
				}
				if (!values.isEmpty()) {
					localizationKey.setLocalizationValues(values).save();
					success = true;
					newLocalizationKey = localizationKey.getKey();
					formWindow.close();
					if (application == null) {
						systemLocalizationProvider.reload();
					}
					onNewKey.fire(newLocalizationKey);
				}
			}
			UiUtils.showSaveNotification(success, applicationInstanceData);
		});
	}

	public void show() {
		formWindow.show();
	}

	public String getNewLocalizationKey() {
		return newLocalizationKey;
	}
}
