package org.teamapps.application.server.system.config;

public class DocumentConversionConfig {
	private boolean active = false;
	private String host = "host";
	private String user = "user";
	private String password = "password";

	public DocumentConversionConfig() {
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
