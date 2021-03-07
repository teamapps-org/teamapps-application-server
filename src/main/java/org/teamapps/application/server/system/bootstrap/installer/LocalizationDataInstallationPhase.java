package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.localization.LocalizationEntry;
import org.teamapps.application.api.localization.LocalizationEntrySet;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.ApplicationInfoDataElement;
import org.teamapps.application.server.system.config.LocalizationConfig;
import org.teamapps.application.server.system.localization.ApplicationScopeLocalizationProvider;
import org.teamapps.application.server.system.localization.LocalizationUtil;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.LocalizationKey;
import org.teamapps.model.controlcenter.LocalizationKeyType;
import org.teamapps.model.controlcenter.LocalizationValue;
import org.teamapps.universaldb.index.numeric.NumericFilter;

import java.util.*;
import java.util.stream.Collectors;

public class LocalizationDataInstallationPhase implements ApplicationInstallationPhase {

	private final LocalizationConfig localizationConfig;

	public LocalizationDataInstallationPhase(LocalizationConfig localizationConfig) {
		this.localizationConfig = localizationConfig;
	}

	@Override
	public void checkApplication(ApplicationInfo applicationInfo) {
		try {
			if (!applicationInfo.getErrors().isEmpty()) {
				return;
			}
			ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
			LocalizationData localizationData = applicationBuilder.getLocalizationData();
			if (localizationData == null) {
				applicationInfo.addError("Missing localization data");
				return;
			}
			List<LocalizationEntrySet> localizationEntrySets = localizationData.getLocalizationEntrySets();
			if (!localizationData.containsAnyLanguage(localizationConfig.getAllowedSourceLanguages())) {
				applicationInfo.addError("Error: no supported language!:" + localizationEntrySets
						.stream()
						.map(LocalizationEntrySet::getLanguage)
						.collect(Collectors.joining(", ")));
				return;
			}
			ApplicationInfoDataElement dataInfo = new ApplicationInfoDataElement();
			dataInfo.setData(String.join("\n", getAllEntries(localizationEntrySets)));
			Application application = applicationInfo.getApplication();

			Map<String, Map<String, String>> localizationMapByKey = localizationData.createLocalizationMapByKey();
			List<LocalizationKey> localizationKeys = application == null ? Collections.emptyList() : LocalizationKey.filter().application(NumericFilter.equalsFilter(application.getId())).execute();
			KeyCompare<String, LocalizationKey> keyCompare = new KeyCompare<>(localizationMapByKey.keySet(), localizationKeys, s -> s, LocalizationKey::getKey);
			List<String> newKeys = keyCompare.getNotInB();
			for (String key : newKeys) {
				Map<String, String> translations = localizationMapByKey.get(key);
				for (Map.Entry<String, String> entry : translations.entrySet()) {
					String language = entry.getKey();
					String original = entry.getValue();
					dataInfo.added(key + " -> " + language + ":" + original);
				}
			}

			List<LocalizationKey> removedKeys = keyCompare.getNotInA();
			removedKeys.stream().flatMap(key -> key.getLocalizationValues().stream()).forEach(value -> {
				dataInfo.removed(value.getLocalizationKey().getKey() + " -> " + value.getLanguage() + ":" + value.getOriginal());
			});
			List<String> existingKeys = keyCompare.getInB();
			for (String key : existingKeys) {
				Map<String, String> translations = localizationMapByKey.get(key);
				LocalizationKey localizationKey = keyCompare.getB(key);
				KeyCompare<String, LocalizationValue> languageCompare = new KeyCompare<>(translations.keySet(), localizationKey.getLocalizationValues(), s -> s, LocalizationValue::getLanguage);
				if (languageCompare.isDifferent()) {
					List<String> newLanguages = languageCompare.getNotInB();
					newLanguages.forEach(language -> dataInfo.added(key + " -> " + language + ":" + translations.get(language)));
					List<LocalizationValue> removedLanguages = languageCompare.getNotInA();
					removedLanguages.forEach(value -> dataInfo.removed(key + " -> " + value.getLanguage() + ":" + value.getOriginal()));
					//todo changed originals
				}
			}
			applicationInfo.setLocalizationData(dataInfo);
		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking localization data:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {
		LocalizationData localizationData = applicationInfo.getApplicationBuilder().getLocalizationData();
		Application application = applicationInfo.getApplication();
		LocalizationKeyType localizationKeyType = LocalizationKeyType.APPLICATION_RESOURCE_KEY;
		LocalizationUtil.synchronizeLocalizationData(localizationData, application, localizationKeyType, localizationConfig.getRequiredLanguages());
	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {
		applicationInfo.getLoadedApplication().setApplicationLocalizationProvider(new ApplicationScopeLocalizationProvider(applicationInfo.getApplication()));
	}


	private static List<String> getAllEntries(List<LocalizationEntrySet> localizationEntrySets) {
		List<String> rows = new ArrayList<>();
		for (LocalizationEntrySet localizationEntrySet : localizationEntrySets) {
			String language = localizationEntrySet.getLanguage();
			for (LocalizationEntry entry : localizationEntrySet.getEntries()) {
				rows.add(language + " -> " + entry.getKey() + ":" + entry.getValue());
			}
		}
		return rows;
	}
}
