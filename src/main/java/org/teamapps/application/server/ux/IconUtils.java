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
package org.teamapps.application.server.ux;

import org.teamapps.icons.Icon;
import org.teamapps.icons.IconLibraryRegistry;
import org.teamapps.icons.IconProvider;
import org.teamapps.icons.SessionIconProvider;
import org.teamapps.ux.session.SessionContext;

public class IconUtils {

	private static SessionIconProvider NO_STYLE_ICON_PROVIDER = new SessionIconProvider(new IconProvider(new IconLibraryRegistry()));


	public static String encodeNoStyle(Icon icon) {
		if (icon == null) {
			return null;
		}
		return NO_STYLE_ICON_PROVIDER.encodeIcon(icon, false);
	}

	public static Icon<?, ?> decodeIconNoStyles(String name) {
		if (name == null) {
			return null;
		}
		try {
			return NO_STYLE_ICON_PROVIDER.decodeIcon(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Icon<?, ?> decodeIcon(String name) {
		if (name == null) {
			return null;
		}
		try {
			return SessionContext.current().getIconProvider().decodeIcon(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
