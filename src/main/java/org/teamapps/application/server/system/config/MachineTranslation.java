package org.teamapps.application.server.system.config;

public class MachineTranslation {

	private boolean active;
	private String deepLKey = "deepKey";
	private String googleKey = "googleKey";

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getDeepLKey() {
		return deepLKey;
	}

	public void setDeepLKey(String deepLKey) {
		this.deepLKey = deepLKey;
	}

	public String getGoogleKey() {
		return googleKey;
	}

	public void setGoogleKey(String googleKey) {
		this.googleKey = googleKey;
	}
}
