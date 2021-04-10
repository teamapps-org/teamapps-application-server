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
package org.teamapps.application.server.system.login;

import org.teamapps.application.server.system.bootstrap.LogoutHandler;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.launcher.ApplicationLauncher;
import org.teamapps.application.server.system.passwordhash.SecurePasswordHash;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.model.controlcenter.User;
import org.teamapps.model.controlcenter.UserAccountStatus;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.text.TextFilter;
import org.teamapps.ux.component.login.LoginWindow;
import org.teamapps.ux.component.panel.ElegantPanel;
import org.teamapps.ux.component.rootpanel.RootPanel;
import org.teamapps.ux.session.SessionContext;

public class LoginHandler {

	private final SystemRegistry systemRegistry;
	private final LogoutHandler logoutHandler;

	public LoginHandler(SystemRegistry systemRegistry, LogoutHandler logoutHandler) {
		this.systemRegistry = systemRegistry;
		this.logoutHandler = logoutHandler;
	}

	public void handleNewSession(SessionContext context) {
		RootPanel rootPanel = context.addRootPanel();
		createLoginView(context, rootPanel);
	}

	public void createLoginView(SessionContext context, RootPanel rootPanel) {
		LoginWindow loginWindow = new LoginWindow();
		ElegantPanel elegantPanel = loginWindow.getElegantPanel();
		rootPanel.setContent(elegantPanel);
		String backgroundUrl = systemRegistry.getSystemConfig().getThemingConfig().getLoginBackgroundUrl();
		context.registerBackgroundImage("login", backgroundUrl, backgroundUrl);
		context.setBackgroundImage("login", 0);

		loginWindow.onLogin.addListener(loginData -> {
			boolean result = false;
			User user = null;
			if (loginData.login != null && loginData.password != null) {
				user = User.filter()
						.login(TextFilter.textEqualsIgnoreCaseFilter(loginData.login))
						.userAccountStatus(EnumFilterType.NOT_EQUALS, UserAccountStatus.INACTIVE)
						.executeExpectSingleton();
				if (user != null) {
					String hash = user.getPassword();
					if (SecurePasswordHash.createDefault().verifyPassword(loginData.password, hash)) {
						result = true;
					}
				}
			}
			if (result) {
				handleSuccessfulLogin(user, rootPanel, context);
			} else {
				loginWindow.setError();
			}
		});
		context.setBackgroundImage("login", 0);
	}

	private void handleSuccessfulLogin(User user, RootPanel rootPanel, SessionContext context) {
		UserSessionData userSessionData = new UserSessionData(user, context, systemRegistry, rootPanel);
		if (systemRegistry.getSessionRegistryHandler() != null) {
			systemRegistry.getSessionRegistryHandler().handleAuthenticatedUser(userSessionData, context);
		}
		new ApplicationLauncher(userSessionData, logoutHandler);
	}
}
