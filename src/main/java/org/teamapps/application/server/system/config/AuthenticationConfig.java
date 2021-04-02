package org.teamapps.application.server.system.config;

public class AuthenticationConfig {

	private boolean allowStoringSecurityTokensOnClient;
	private boolean allowPasswordReset;
	private boolean allowRegistration;

	public boolean isAllowStoringSecurityTokensOnClient() {
		return allowStoringSecurityTokensOnClient;
	}

	public void setAllowStoringSecurityTokensOnClient(boolean allowStoringSecurityTokensOnClient) {
		this.allowStoringSecurityTokensOnClient = allowStoringSecurityTokensOnClient;
	}

	public boolean isAllowPasswordReset() {
		return allowPasswordReset;
	}

	public void setAllowPasswordReset(boolean allowPasswordReset) {
		this.allowPasswordReset = allowPasswordReset;
	}

	public boolean isAllowRegistration() {
		return allowRegistration;
	}

	public void setAllowRegistration(boolean allowRegistration) {
		this.allowRegistration = allowRegistration;
	}
}
