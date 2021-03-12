package org.teamapps.application.server.ux.localize;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.ux.IconUtils;
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
import java.util.stream.Collectors;

public class LocalizationUiUtils {

	public static ComboBox<String> createLocalizationKeyCombo(Template template, ApplicationLocalizationProvider localizationProvider) {
		return createLocalizationKeyCombo(template, localizationProvider, null);
	}

	public static ComboBox<String> createLocalizationKeyCombo(Template template, ApplicationLocalizationProvider localizationProvider, Application application) {
		ComboBox<String> comboBox = new ComboBox<>(template);
		comboBox.setPropertyProvider((s, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			LocalizationKey localizationKey = LocalizationKey.filter().key(TextFilter.textEqualsFilter(s)).executeExpectSingleton();
			map.put(BaseTemplate.PROPERTY_ICON, getLocalizationKeyIcon(localizationKey));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(localizationKey.getKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationKey.getKey());
			return map;
		});
		comboBox.setRecordToStringFunction(localizationProvider::getLocalized);
		comboBox.setModel(query -> {
			List<LocalizationKey> keys = new ArrayList<>();
			keys.addAll(application != null ? LocalizationKey.filter().application(NumericFilter.equalsFilter(application.getId())).execute() : Collections.emptyList());
			keys.addAll(LocalizationKey.filter().localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.DICTIONARY_KEY).execute());
			keys.addAll(LocalizationKey.filter().localizationKeyType(EnumFilterType.EQUALS, LocalizationKeyType.SYSTEM_KEY).execute());
			if (query == null || query.isBlank()) {
				return keys.stream()
						.limit(50)
						.map(LocalizationKey::getKey)
						.collect(Collectors.toList());
			} else {
				String q = query.toLowerCase();
				return keys.stream()
						.filter(key -> key.getKey().toLowerCase().contains(q) || localizationProvider.getLocalized(key.getKey()).toLowerCase().contains(q))
						.limit(50)
						.map(LocalizationKey::getKey)
						.collect(Collectors.toList());
			}
		});
		return comboBox;
	}

	public static Icon getLocalizationKeyIcon(LocalizationKey key) {
		return switch (key.getLocalizationKeyType()) {
			case APPLICATION_RESOURCE_KEY -> IconUtils.decodeIcon(key.getApplication().getIcon());
			case DICTIONARY_KEY -> ApplicationIcons.DICTIONARY;
			case REPORTING_KEY -> ApplicationIcons.DOCUMENT_NOTEBOOK;
			case SYSTEM_KEY -> ApplicationIcons.SYSTEM;
			default -> null;
		};
	}
}
