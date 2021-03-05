package org.teamapps.application.server.system.session;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.launcher.MobileApplicationNavigation;
import org.teamapps.application.server.system.localization.UserLocalizationProvider;
import org.teamapps.application.server.system.privilege.PrivilegeApplicationKey;
import org.teamapps.application.server.system.privilege.UserPrivileges;
import org.teamapps.icons.Icon;
import org.teamapps.icons.SessionIconProvider;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.component.rootpanel.RootPanel;
import org.teamapps.ux.session.SessionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSessionData {

	private final User user;
	private final SessionContext context;
	private final SystemRegistry registry;
	private final RootPanel rootPanel;
	private UserPrivileges userPrivileges;
	private final SessionUser sessionUser;
	private final SessionIconProvider iconProvider;
	private final UserLocalizationProvider dictionary;
	private final Map<Application, ApplicationLocalizationProvider> localizationProviderByApplication = new HashMap<>();

	public UserSessionData(User user, SessionContext context, SystemRegistry registry, RootPanel rootPanel) {
		this.user = user;
		this.context = context;
		this.registry = registry;
		this.rootPanel = rootPanel;
		this.userPrivileges = new UserPrivileges(user, registry);
		this.sessionUser = new SessionUserImpl(user, context);
		this.dictionary = new UserLocalizationProvider(sessionUser.getRankedLanguages(), registry.getDictionary(), registry.getSystemDictionary());
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
		List<String> rankedLanguages = sessionUser.getRankedLanguages();
		LoadedApplication loadedApplication = registry.getLoadedApplication(application);
		return new UserLocalizationProvider(rankedLanguages, registry.getDictionary(), registry.getSystemDictionary(), loadedApplication.getApplicationLocalizationProvider());
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

	public SessionUser getSessionUser() {
		return sessionUser;
	}

	public SessionIconProvider getIconProvider() {
		return iconProvider;
	}

	public ApplicationLocalizationProvider getDictionary() {
		return dictionary;
	}

	public UserPrivileges getUserPrivileges() {
		return userPrivileges;
	}

	public void invalidate() {
		userPrivileges = null;
	}
}
