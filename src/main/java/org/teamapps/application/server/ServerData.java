/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2025 TeamApps.org
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
package org.teamapps.application.server;

public class ServerData {

	public static void setServerMode(ServerMode mode) {
		if (mode == null) {
			throw new RuntimeException("ERROR: trying to update invalid server mode");
		}
		if (serverMode != null) {
			throw new RuntimeException("ERROR: trying to update existing server mode");
		}
		serverMode = mode;
	}


	private static void checkServerData() {
		if (serverMode == null) {
			throw new RuntimeException("ERROR: trying to access unset server data");
		}
	}

	private static ServerMode serverMode;

	public static ServerMode getServerMode() {
		checkServerData();
		return serverMode;
	}

	public static boolean isProductionMode() {
		return getServerMode() == ServerMode.PRODUCTION;
	}


}
