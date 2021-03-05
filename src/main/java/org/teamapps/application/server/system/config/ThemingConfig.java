package org.teamapps.application.server.system.config;

public class ThemingConfig {

	//todo move this to db - config ui

	private String loginBackgroundUrl = "/resources/backgrounds/login.jpg";
	private String defaultApplicationBackgroundUrl = "/resources/backgrounds/default-bl.jpg";

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
