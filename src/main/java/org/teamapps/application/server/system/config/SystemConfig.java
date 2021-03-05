package org.teamapps.application.server.system.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("config")
public class SystemConfig {

	private ThemingConfig themingConfig = new ThemingConfig();
	private AuthenticationConfig authenticationConfig = new AuthenticationConfig();
	private MachineTranslation machineTranslation = new MachineTranslation();
	private LocalizationConfig localizationConfig = new LocalizationConfig();

	public MachineTranslation getMachineTranslation() {
		return machineTranslation;
	}

	public void setMachineTranslation(MachineTranslation machineTranslation) {
		this.machineTranslation = machineTranslation;
	}

	public LocalizationConfig getLocalizationConfig() {
		return localizationConfig;
	}

	public void setLocalizationConfig(LocalizationConfig localizationConfig) {
		this.localizationConfig = localizationConfig;
	}

	public ThemingConfig getThemingConfig() {
		return themingConfig;
	}

	public void setThemingConfig(ThemingConfig themingConfig) {
		this.themingConfig = themingConfig;
	}

	public AuthenticationConfig getAuthenticationConfig() {
		return authenticationConfig;
	}

	public void setAuthenticationConfig(AuthenticationConfig authenticationConfig) {
		this.authenticationConfig = authenticationConfig;
	}
}
