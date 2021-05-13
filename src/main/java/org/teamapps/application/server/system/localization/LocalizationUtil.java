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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.index.text.TextFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LocalizationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static List<String> allowedSourceTranslationLanguages = Arrays.asList("bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "hu", "it", "ja", "lt", "lv", "nl", "pl", "pt", "ro", "ru", "sk", "sl", "sv", "zh");

	public static void synchronizeLocalizationData(LocalizationData localizationData, Application application, LocalizationKeyType localizationKeyType, List<String> requiredLanguages) {
		Map<String, Map<String, String>> localizationMapByKey = localizationData.createLocalizationMapByKey();
		int appIdFilter = 0;
		if (application != null) {
			appIdFilter = application.getId();
		}
		List<LocalizationKey> localizationKeys = LocalizationKey.filter().application(NumericFilter.equalsFilter(appIdFilter)).execute();
		KeyCompare<String, LocalizationKey> keyCompare = new KeyCompare<>(localizationMapByKey.keySet(), localizationKeys, s -> s, LocalizationKey::getKey);
		List<String> newKeys = keyCompare.getNotInB();

		LocalizationTopic topic = getTopic(localizationKeyType, application);
		for (String key : newKeys) {
			LocalizationKey localizationKey = LocalizationKey.create()
					.setApplication(application)
					.setLocalizationKeyType(localizationKeyType)
					.setTopics(topic)
					.setUsed(true)
					.setKey(key)
					.save();
			Map<String, String> translations = localizationMapByKey.get(key);
			for (Map.Entry<String, String> entry : translations.entrySet()) {
				String language = entry.getKey();
				String original = entry.getValue();
				LocalizationValue.create()
						.setLocalizationKey(localizationKey)
						.setLanguage(language)
						.setOriginal(original)
						.setCurrentDisplayValue(original)
						.setMachineTranslationState(MachineTranslationState.NOT_NECESSARY)
						.setTranslationState(TranslationState.NOT_NECESSARY)
						.setTranslationVerificationState(TranslationVerificationState.NOT_NECESSARY)
						.save();
			}
		}

		List<LocalizationKey> removedKeys = keyCompare.getNotInA();
		removedKeys.forEach(key -> key.setUsed(false).save());

		List<String> existingKeys = keyCompare.getInB();
		for (String key : existingKeys) {
			//todo check if used = false -> set to true!
			Map<String, String> translations = localizationMapByKey.get(key);
			LocalizationKey localizationKey = keyCompare.getB(key);
			KeyCompare<String, LocalizationValue> languageCompare = new KeyCompare<>(translations.keySet(), localizationKey.getLocalizationValues(), s -> s, LocalizationValue::getLanguage);
			if (languageCompare.isDifferent()) {
				List<String> newLanguages = languageCompare.getNotInB();
				newLanguages.forEach(language -> {
					String original = translations.get(language);
					LocalizationValue.create()
							.setLocalizationKey(localizationKey)
							.setLanguage(language)
							.setOriginal(original)
							.setCurrentDisplayValue(original)
							.setMachineTranslationState(MachineTranslationState.NOT_NECESSARY)
							.setTranslationState(TranslationState.NOT_NECESSARY)
							.setTranslationVerificationState(TranslationVerificationState.NOT_NECESSARY)
							.save();
				});
				List<LocalizationValue> removedLanguages = languageCompare.getNotInA();
				for (LocalizationValue value : removedLanguages) {
					value.setOriginal(null);
					if (value.getAdminKeyOverride() == null && value.getAdminLocalOverride() == null && value.getMachineTranslation() == null && value.getTranslation() == null) {
						value
								.setMachineTranslationState(MachineTranslationState.TRANSLATION_REQUESTED)
								.setTranslationState(TranslationState.TRANSLATION_REQUESTED)
								.setTranslationVerificationState(TranslationVerificationState.NOT_YET_TRANSLATED);

					}
					value.save();
				}
			}
			//checking all existing values if the original changed
			for (LocalizationValue localizationValue : languageCompare.getInA()) {
				String original = translations.get(localizationValue.getLanguage());
				if (original != null && !original.isBlank() && !original.equals(localizationValue.getOriginal())) {
					localizationValue.setOriginal(original).save();
					localizationValue.getLocalizationKey().getLocalizationValues().stream()
							.filter(value -> !value.equals(localizationValue))
							.filter(value -> value.getOriginal() == null)
							.filter(value -> value.getAdminLocalOverride() == null)
							.filter(value -> value.getAdminKeyOverride() == null)
							.forEach(value -> value
									.setMachineTranslationState(MachineTranslationState.TRANSLATION_REQUESTED)
									.setTranslationState(TranslationState.TRANSLATION_REQUESTED)
									.setTranslationVerificationState(TranslationVerificationState.NOT_YET_TRANSLATED)
									.save());
				}
			}
		}

		//create translation requests
		localizationKeys = LocalizationKey.filter().application(NumericFilter.equalsFilter(appIdFilter)).execute();
		for (LocalizationKey key : localizationKeys) {
			Map<String, LocalizationValue> valueByLanguage = key.getLocalizationValues().stream().collect(Collectors.toMap(LocalizationValue::getLanguage, v -> v));
			for (String language : requiredLanguages) {
				if (!valueByLanguage.containsKey(language)) {
					LocalizationValue.create()
							.setLocalizationKey(key)
							.setLanguage(language)
							.setMachineTranslationState(MachineTranslationState.TRANSLATION_REQUESTED)
							.setTranslationState(TranslationState.TRANSLATION_REQUESTED)
							.setTranslationVerificationState(TranslationVerificationState.NOT_YET_TRANSLATED)
							.save();
				}
			}
		}
	}

	public static void translateAllApplicationValues(TranslationService translationService, Application application) {
		if (translationService == null) {
			return;
		}
		List<LocalizationValue> translationRequests = LocalizationValue
				.filter()
				.machineTranslationState(EnumFilterType.EQUALS, MachineTranslationState.TRANSLATION_REQUESTED)
				.execute().stream()
				.filter(value -> value.getLocalizationKey().getApplication().equals(application))
				.collect(Collectors.toList());
		ExecutorService executor = Executors.newFixedThreadPool(10);
		translationRequests.forEach(localizationValue -> executor.submit(() -> translateLocalizationValue(localizationValue, translationService)));
		executor.shutdown();
	}

	public static void translateAllDictionaryValues(TranslationService translationService) {
		if (translationService == null) {
			return;
		}
		List<LocalizationValue> translationRequests = LocalizationValue
				.filter()
				.machineTranslationState(EnumFilterType.EQUALS, MachineTranslationState.TRANSLATION_REQUESTED)
				.execute().stream()
				.filter(value -> value.getLocalizationKey().getLocalizationKeyType() == LocalizationKeyType.DICTIONARY_KEY)
				.collect(Collectors.toList());
		ExecutorService executor = Executors.newFixedThreadPool(10);
		translationRequests.forEach(localizationValue -> executor.submit(() -> translateLocalizationValue(localizationValue, translationService)));
		executor.shutdown();
	}

	public static void translateAllValues(TranslationService translationService) {
		if (translationService == null) {
			return;
		}
		List<LocalizationValue> translationRequests = LocalizationValue.filter()
				.machineTranslationState(EnumFilterType.EQUALS, MachineTranslationState.TRANSLATION_REQUESTED)
				.execute();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		translationRequests.forEach(localizationValue -> executor.submit(() -> translateLocalizationValue(localizationValue, translationService)));
		executor.shutdown();
	}

	public static void translateLocalizationValue(LocalizationValue missingTranslationValue, TranslationService translationService) {
		LocalizationValue adminValue = missingTranslationValue.getLocalizationKey().getLocalizationValues().stream()
				.filter(value -> value.getAdminKeyOverride() != null)
				.filter(value -> allowedSourceTranslationLanguages.contains(value.getLanguage()))
				.findFirst()
				.orElse(null);
		if (adminValue != null && translationService.canTranslate(adminValue.getLanguage(), missingTranslationValue.getLanguage())) {
			String translation = translationService.translate(adminValue.getAdminKeyOverride(), adminValue.getLanguage(), missingTranslationValue.getLanguage());
			if (translation != null) {
				translation = firstUpperIfSourceUpper(adminValue.getAdminKeyOverride(), translation);
				missingTranslationValue
						.setMachineTranslation(translation)
						.setMachineTranslationState(MachineTranslationState.OK)
						.setCurrentDisplayValue(getDisplayValue(missingTranslationValue))
						.save();
				return;
			}
		}

		Map<String, LocalizationValue> localizationValueByLanguage = missingTranslationValue.getLocalizationKey().getLocalizationValues().stream()
				.filter(value -> !value.equals(missingTranslationValue))
				.filter(value -> value.getOriginal() != null)
				.collect(Collectors.toMap(LocalizationValue::getLanguage, v -> v));

		for (String language : allowedSourceTranslationLanguages) {
			LocalizationValue sourceValue = localizationValueByLanguage.get(language);
			if (sourceValue != null && translationService.canTranslate(language, missingTranslationValue.getLanguage())) {
				String translationSourceText = getTranslationSourceText(sourceValue);
				if (translationSourceText != null && !translationSourceText.isBlank()) {
					String translation = translationService.translate(translationSourceText, language, missingTranslationValue.getLanguage());
					if (translation != null) {
						translation = firstUpperIfSourceUpper(translationSourceText, translation);
						System.out.println("Translate (" + language + "->" + missingTranslationValue.getLanguage() + "): " + translationSourceText + " -> " + translation);
						missingTranslationValue
								.setMachineTranslation(translation)
								.setMachineTranslationState(MachineTranslationState.OK)
								.setCurrentDisplayValue(getDisplayValue(missingTranslationValue))
								.save();
						return;
					}
				}
			}
		}
	}

	private static String getTranslationSourceText(LocalizationValue localizationValue) {
		String value = localizationValue.getAdminKeyOverride();
		if (value == null) {
			value = localizationValue.getAdminLocalOverride();
		}
		if (value == null) {
			value = localizationValue.getOriginal();
		}
		return value;
	}

	private static String getDisplayValue(LocalizationValue localizationValue) {
		String value = localizationValue.getAdminKeyOverride();
		if (value == null) {
			value = localizationValue.getAdminLocalOverride();
		}
		if (value == null) {
			value = localizationValue.getOriginal();
		}
		if (value == null) {
			value = localizationValue.getTranslation();
		}
		if (value == null) {
			value = localizationValue.getMachineTranslation();
		}
		return value;
	}

	private static String firstUpperIfSourceUpper(String source, String text) {
		if (source == null || text == null || source.isEmpty() || text.isEmpty()) {
			return text;
		}
		char c = source.substring(0, 1).charAt(0);
		if (Character.isUpperCase(c)) {
			return text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			return text;
		}
	}

	private static LocalizationTopic getTopic(LocalizationKeyType keyType, Application application) {
		return switch (keyType) {
			case APPLICATION_RESOURCE_KEY -> getOrCreateTopic(application.getName(), application.getIcon(), application);
			case DICTIONARY_KEY -> getOrCreateTopic("Dictionary", IconUtils.encodeNoStyle(ApplicationIcons.DICTIONARY), application);
			case SYSTEM_KEY -> getOrCreateTopic("System", IconUtils.encodeNoStyle(ApplicationIcons.SYSTEM), application);
			case REPORTING_KEY -> getOrCreateTopic("Reporting", IconUtils.encodeNoStyle(ApplicationIcons.FORM), application);
		};
	}

	private static LocalizationTopic getOrCreateTopic(String name, String icon, Application application) {
		LocalizationTopic topic = LocalizationTopic.filter().title(TextFilter.textEqualsFilter(name)).executeExpectSingleton();
		if (topic == null) {
			topic = LocalizationTopic.create()
					.setTitle(name)
					.setApplication(application)
					.setIcon(icon)
					.save();
		}
		return topic;
	}

	public static File createTranslationResourceFiles() throws IOException {
		Map<String, List<LocalizationValue>> valuesByDomain = LocalizationValue.getAll().stream().filter(value -> value.getCurrentDisplayValue() != null).collect(Collectors.groupingBy(value -> {
			if (value.getLocalizationKey().getApplication() != null) {
				return value.getLocalizationKey().getApplication().getName();
			} else {
				return value.getLocalizationKey().getLocalizationKeyType().name();
			}
		}));

		File zipFile = File.createTempFile("temp", ".zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (Map.Entry<String, List<LocalizationValue>> domainEntry : valuesByDomain.entrySet()) {
			String applicationOrType = domainEntry.getKey();
			zos.putNextEntry(new ZipEntry(applicationOrType + "/"));
			zos.closeEntry();

			Map<String, List<LocalizationValue>> valueMap = domainEntry.getValue().stream().collect(Collectors.groupingBy(LocalizationValue::getLanguage));
			for (Map.Entry<String, List<LocalizationValue>> entry : valueMap.entrySet()) {
				String language = entry.getKey();
				String fileName = applicationOrType;
				if (applicationOrType.equals("DICTIONARY_KEY")) {
					fileName = "dictionary";
				}
				zos.putNextEntry(new ZipEntry(applicationOrType + "/" + fileName + "_" + language + ".properties"));
				List<LocalizationValue> values = entry.getValue().stream().sorted(Comparator.comparing(o -> o.getLocalizationKey().getKey())).collect(Collectors.toList());
				StringBuilder sb = new StringBuilder();
				for (LocalizationValue value : values) {
					sb.append(value.getLocalizationKey().getKey())
							.append("=")
							.append(value.getCurrentDisplayValue())
							.append("\n");
				}
				zos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
			}
		}
		zos.close();
		fos.close();
		return zipFile;
	}

}
