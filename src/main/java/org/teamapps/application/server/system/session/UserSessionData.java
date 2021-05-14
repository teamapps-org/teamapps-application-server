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
package org.teamapps.application.server.system.session;

import org.teamapps.application.api.desktop.ApplicationDesktop;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.launcher.MobileApplicationNavigation;
import org.teamapps.application.server.system.localization.SessionLocalizationProvider;
import org.teamapps.application.server.system.privilege.PrivilegeApplicationKey;
import org.teamapps.application.server.system.privilege.UserPrivileges;
import org.teamapps.icons.Icon;
import org.teamapps.icons.SessionIconProvider;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.rootpanel.RootPanel;
import org.teamapps.ux.session.SessionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class UserSessionData {

	private final User user;
	private final SessionContext context;
	private final SystemRegistry registry;
	private final RootPanel rootPanel;
	private UserPrivileges userPrivileges;
	private final SessionUser sessionUser;
	private final SessionIconProvider iconProvider;
	private final SessionLocalizationProvider sessionLocalizationProvider;
	private final Map<Application, ApplicationLocalizationProvider> localizationProviderByApplication = new HashMap<>();
	private Supplier<ApplicationDesktop> applicationDesktopSupplier;
	private Function<Component, Component> rootWrapperComponentFunction;

	public UserSessionData(User user, SessionContext context, SystemRegistry registry, RootPanel rootPanel) {
		this.user = user;
		this.context = context;
		this.registry = registry;
		this.rootPanel = rootPanel;
		this.userPrivileges = new UserPrivileges(user, registry);
		this.sessionUser = new SessionUserImpl(user, context);
		this.sessionLocalizationProvider = new SessionLocalizationProvider(sessionUser.getRankedLanguages(), registry.getDictionary(), registry.getSystemDictionary());
		this.iconProvider = context.getIconProvider();
	}

	public ManagedApplicationSessionData createManageApplicationSessionData(ManagedApplication managedApplication, MobileApplicationNavigation mobileNavigation) {
		return new ManagedApplicationSessionData(this, managedApplication, mobileNavigation);
	}

	public ApplicationPrivilegeProvider getApplicationPrivilegeProvider(ManagedApplication managedApplication) {
		return getUserPrivileges().getApplicationPrivilegeProvider(PrivilegeApplicationKey.create(managedApplication));
	}

	public ApplicationLocalizationProvider getApplicationLocalizationProvider(Application application) {
		if (!localizationProviderByApplication.containsKey(application)) {
			ApplicationLocalizationProvider applicationLocalizationProvider = createApplicationLocalizationProvider(application);
			localizationProviderByApplication.put(application, applicationLocalizationProvider);
		}
		return localizationProviderByApplication.get(application);
	}

	private ApplicationLocalizationProvider createApplicationLocalizationProvider(Application application) {
		return sessionLocalizationProvider.createApplicationLocalizationProvider(application);
	}

	public Icon<?, ?> decodeIcon(String name) {
		if (name == null) {
			return null;
		}
		try {
			return context.getIconProvider().decodeIcon(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public User getUser() {
		return user;
	}

	public SessionContext getContext() {
		return context;
	}

	public SystemRegistry getRegistry() {
		return registry;
	}

	public RootPanel getRootPanel() {
		return rootPanel;
	}

	public void setRooWrapperComponentFunction(Function<Component, Component> rootWrapperComponentFunction) {
		this.rootWrapperComponentFunction = rootWrapperComponentFunction;
		rootPanel.setContent(rootWrapperComponentFunction.apply(rootPanel.getContent()));
	}

	public void setRootComponent(Component component) {
		if (rootWrapperComponentFunction != null) {
			rootPanel.setContent(rootWrapperComponentFunction.apply(component));
		} else {
			rootPanel.setContent(component);
		}
	}

	public SessionUser getSessionUser() {
		return sessionUser;
	}

	public SessionIconProvider getIconProvider() {
		return iconProvider;
	}

	public SessionLocalizationProvider getLocalizationProvider() {
		return sessionLocalizationProvider;
	}

	public UserPrivileges getUserPrivileges() {
		return userPrivileges;
	}

	public void invalidate() {
		userPrivileges = null;
	}

	public List<String> getRankedLanguages() {
		return sessionUser.getRankedLanguages();
	}

	public Supplier<ApplicationDesktop> getApplicationDesktopSupplier() {
		return applicationDesktopSupplier;
	}

	public void setApplicationDesktopSupplier(Supplier<ApplicationDesktop> applicationDesktopSupplier) {
		this.applicationDesktopSupplier = applicationDesktopSupplier;
	}
}
