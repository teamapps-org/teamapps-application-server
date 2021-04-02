/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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
