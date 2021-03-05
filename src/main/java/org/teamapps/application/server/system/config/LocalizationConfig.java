package org.teamapps.application.server.system.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalizationConfig {

	private List<String> allowedSourceLanguages = new ArrayList<>(Arrays.asList("en", "de", "fr", "es", "pt", "nl", "it", "pl", "ru"));
	private List<String> requiredLanguages = new ArrayList<>(Arrays.asList("en", "de", "fr", "es"));

	public List<String> getAllowedSourceLanguages() {
		return allowedSourceLanguages;
	}

	public void setAllowedSourceLanguages(List<String> allowedSourceLanguages) {
		this.allowedSourceLanguages = allowedSourceLanguages;
	}

	public List<String> getRequiredLanguages() {
		return requiredLanguages;
	}

	public void setRequiredLanguages(List<String> requiredLanguages) {
		this.requiredLanguages = requiredLanguages;
	}
}
