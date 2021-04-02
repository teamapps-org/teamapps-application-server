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
package org.teamapps.application.server.ux.window;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.toolbar.Toolbar;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.window.Window;
import org.teamapps.ux.session.SessionContext;

public class WindowUtils {

	public static Window createWindow(Icon icon, String title) {
		int width = (int) Math.max(800, Math.min(1600, SessionContext.current().getClientInfo().getViewPortWidth() * 0.8f));
		int height = (int) Math.max(600, Math.min(1200, SessionContext.current().getClientInfo().getScreenHeight() * 0.8f));
		Window window = new Window(icon, title, width, height, null);
		window.setToolbar(new Toolbar());
		window.setCloseable(true);
		window.setCloseOnEscape(true);
		window.setMaximizable(true);
		return window;
	}

	public static void addCancelCloseButton(Window window, ApplicationInstanceData applicationInstanceData) {
		Toolbar toolbar = window.getToolbar();
		if (toolbar == null) {
			toolbar = new Toolbar();
			window.setToolbar(toolbar);
		}
		ToolbarButtonGroup buttonGroup = toolbar.addButtonGroup(new ToolbarButtonGroup());
		ToolbarButton closeButton = ToolbarButton.create(ApplicationIcons.ERROR, applicationInstanceData.getLocalized(Dictionary.CANCEL), applicationInstanceData.getLocalized(Dictionary.CANCEL_AND_CLOSE_WINDOW));
		closeButton.onClick.addListener((Runnable) window::close);
		buttonGroup.addButton(closeButton);

	}

}
