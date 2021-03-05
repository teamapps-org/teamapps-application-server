package org.teamapps.application.server.system.bootstrap;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.application.ApplicationPerspectiveBuilder;
import org.teamapps.application.api.application.PerspectiveBuilder;
import org.teamapps.application.server.system.localization.ApplicationScopeLocalizationProvider;
import org.teamapps.application.server.system.privilege.ApplicationScopePrivilegeProvider;
import org.teamapps.model.controlcenter.Application;

import java.util.Collections;
import java.util.List;

public class LoadedApplication {

	private final Application application;
	private final ApplicationBuilder applicationBuilder;
	private final ClassLoader applicationClassLoader;
	private final boolean unmanagedPerspectives;
	private ApplicationScopeLocalizationProvider applicationScopeLocalizationProvider;
	private ApplicationScopePrivilegeProvider applicationScopePrivilegeProvider;

	public LoadedApplication(Application application, ApplicationBuilder applicationBuilder, ClassLoader applicationClassLoader, boolean unmanagedPerspectives) {
		this.application = application;
		this.applicationBuilder = applicationBuilder;
		this.applicationClassLoader = applicationClassLoader;
		this.unmanagedPerspectives = unmanagedPerspectives;
	}

	public List<PerspectiveBuilder> getPerspectiveBuilders() {
		if (unmanagedPerspectives) {
			return Collections.emptyList();
		} else {
			ApplicationPerspectiveBuilder applicationPerspectiveBuilder = (ApplicationPerspectiveBuilder) applicationBuilder;
			return applicationPerspectiveBuilder.getPerspectiveBuilders();
		}
	}

	public PerspectiveBuilder getPerspectiveBuilder(String name) {
		return getPerspectiveBuilders().stream()
				.filter(perspective -> perspective.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	public ApplicationScopeLocalizationProvider getApplicationLocalizationProvider() {
		return applicationScopeLocalizationProvider;
	}

	public void setApplicationLocalizationProvider(ApplicationScopeLocalizationProvider applicationScopeLocalizationProvider) {
		this.applicationScopeLocalizationProvider = applicationScopeLocalizationProvider;
	}

	public ApplicationScopePrivilegeProvider getAppPrivilegeProvider() {
		return applicationScopePrivilegeProvider;
	}

	public void setAppPrivilegeProvider(ApplicationScopePrivilegeProvider applicationScopePrivilegeProvider) {
		this.applicationScopePrivilegeProvider = applicationScopePrivilegeProvider;
	}

	public Application getApplication() {
		return application;
	}

	public ApplicationBuilder getApplicationBuilder() {
		return applicationBuilder;
	}

	public ClassLoader getApplicationClassLoader() {
		return applicationClassLoader;
	}

	public boolean isUnmanagedPerspectives() {
		return unmanagedPerspectives;
	}
}
