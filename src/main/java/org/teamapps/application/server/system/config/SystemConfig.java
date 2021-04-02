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
