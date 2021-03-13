package org.teamapps.application.server.system.localization;

import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.index.text.TextFilter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocalizationUtil {

	private static List<String> allowedSourceTranslationLanguages = Arrays.asList("en", "de", "fr", "es", "pt", "nl", "it", "pl", "ru");

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
		List<LocalizationValue> translationRequests = LocalizationValue.filter().machineTranslationState(EnumFilterType.EQUALS, MachineTranslationState.TRANSLATION_REQUESTED).execute();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		translationRequests.forEach(localizationValue -> executor.submit(() -> translateLocalizationValue(localizationValue, translationService)));
		executor.shutdown();
	}

	public static void translateLocalizationValue(LocalizationValue localizationValue, TranslationService translationService) {
		LocalizationValue adminValue = localizationValue.getLocalizationKey().getLocalizationValues().stream()
				.filter(value -> value.getAdminKeyOverride() != null)
				.filter(value -> allowedSourceTranslationLanguages.contains(value.getLanguage()))
				.findFirst()
				.orElse(null);
		if (adminValue != null && translationService.canTranslate(adminValue.getLanguage(), localizationValue.getLanguage())) {
			String translation = translationService.translate(adminValue.getAdminKeyOverride(), adminValue.getLanguage(), localizationValue.getLanguage());
			if (translation != null) {
				translation = firstUpperIfSourceUpper(adminValue.getAdminKeyOverride(), translation);
				localizationValue
						.setTranslation(translation)
						.setMachineTranslationState(MachineTranslationState.OK)
						.setCurrentDisplayValue(getDisplayValue(localizationValue))
						.save();
				return;
			}
		}

		Map<String, LocalizationValue> localizationValueByLanguage = localizationValue.getLocalizationKey().getLocalizationValues().stream()
				.filter(value -> !value.equals(localizationValue))
				.filter(value -> value.getOriginal() != null)
				.collect(Collectors.toMap(LocalizationValue::getLanguage, v -> v));

		for (String language : allowedSourceTranslationLanguages) {
			LocalizationValue value = localizationValueByLanguage.get(language);
			if (value != null && translationService.canTranslate(language, localizationValue.getLanguage())) {
				String translationSourceText = getTranslationSourceText(localizationValue);
				String translation = translationService.translate(translationSourceText, language, localizationValue.getLanguage());
				if (translation != null) {
					translation = firstUpperIfSourceUpper(translationSourceText, translation);
					localizationValue
							.setTranslation(translation)
							.setMachineTranslationState(MachineTranslationState.OK)
							.setCurrentDisplayValue(getDisplayValue(localizationValue))
							.save();
					return;
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

}
