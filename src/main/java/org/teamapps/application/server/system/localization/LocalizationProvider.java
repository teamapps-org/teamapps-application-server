package org.teamapps.application.server.system.localization;

import java.util.List;

public interface LocalizationProvider {

	String getLocalizationValue(String key, List<String> languagePriorityOrder);
}
