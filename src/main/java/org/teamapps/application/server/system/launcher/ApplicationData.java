package org.teamapps.application.server.system.launcher;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.session.ManagedApplicationSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;

public class ApplicationData {


	private final ManagedApplication managedApplication;
	private LoadedApplication loadedApplication;
	private ManagedApplicationSessionData applicationSessionData;
	private final Icon icon;
	private final String title;
	private final String description;
	private final int applicationPosition;

	public ApplicationData(ManagedApplication managedApplication, LoadedApplication loadedApplication, ManagedApplicationSessionData applicationSessionData) {
		this.managedApplication = managedApplication;
		this.loadedApplication = loadedApplication;
		this.applicationSessionData = applicationSessionData;
		this.icon = managedApplication.getIcon() != null ? IconUtils.decodeIcon(managedApplication.getIcon()) : loadedApplication.getApplicationBuilder().getApplicationIcon();
		ApplicationLocalizationProvider localizationProvider = applicationSessionData.getMainApplicationLocalizationProvider();
		this.title = managedApplication.getTitleKey() != null ? localizationProvider.getLocalized(managedApplication.getTitleKey()) : localizationProvider.getLocalized(loadedApplication.getApplicationBuilder().getApplicationTitleKey());
		this.description = managedApplication.getDescriptionKey() != null ? localizationProvider.getLocalized(managedApplication.getDescriptionKey()) : localizationProvider.getLocalized(loadedApplication.getApplicationBuilder().getApplicationDescriptionKey());
		this.applicationPosition = managedApplication.getListingPosition();
	}

	public void reloadApplicationData(UserSessionData userSessionData) {
		Application application = managedApplication.getMainApplication();
		loadedApplication = userSessionData.getRegistry().getLoadedApplication(application);
		applicationSessionData = userSessionData.createManageApplicationSessionData(managedApplication, new MobileApplicationNavigation());
	}

	public ManagedApplication getManagedApplication() {
		return managedApplication;
	}

	public LoadedApplication getLoadedApplication() {
		return loadedApplication;
	}

	public ManagedApplicationSessionData getApplicationSessionData() {
		return applicationSessionData;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Integer getApplicationPosition() {
		return applicationPosition;
	}
}
