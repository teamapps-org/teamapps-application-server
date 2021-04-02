package org.teamapps.application.server.system.config;

public class ThemingConfig {

	private boolean customApplicationBackground;

	private String loginBackgroundUrl = "/resources/backgrounds/login.jpg";
	private String defaultApplicationBackgroundUrl = "/static/backgrounds/default.jpg";

	public boolean isCustomApplicationBackground() {
		return customApplicationBackground;
	}

	public void setCustomApplicationBackground(boolean customApplicationBackground) {
		this.customApplicationBackground = customApplicationBackground;
	}

	public String getLoginBackgroundUrl() {
		return loginBackgroundUrl;
	}

	public void setLoginBackgroundUrl(String loginBackgroundUrl) {
		this.loginBackgroundUrl = loginBackgroundUrl;
	}

	public String getDefaultApplicationBackgroundUrl() {
		return defaultApplicationBackgroundUrl;
	}

	public void setDefaultApplicationBackgroundUrl(String defaultApplicationBackgroundUrl) {
		this.defaultApplicationBackgroundUrl = defaultApplicationBackgroundUrl;
	}
}
