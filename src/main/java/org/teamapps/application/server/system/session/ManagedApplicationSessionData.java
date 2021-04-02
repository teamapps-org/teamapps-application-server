package org.teamapps.application.server.system.session;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.PerspectiveBuilder;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.launcher.MobileApplicationNavigation;
import org.teamapps.application.server.system.launcher.MobileAssembler;
import org.teamapps.application.server.system.localization.UserLocalizationProvider;
import org.teamapps.application.server.system.privilege.AllowAllPrivilegeProvider;
import org.teamapps.application.server.system.privilege.PrivilegeApplicationKey;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.ManagedApplicationPerspective;
import org.teamapps.model.controlcenter.OrganizationFieldView;
import org.teamapps.model.controlcenter.UserAccountStatus;
import org.teamapps.ux.application.ResponsiveApplication;
import org.teamapps.ux.application.assembler.DesktopApplicationAssembler;
import org.teamapps.ux.session.SessionContext;

public class ManagedApplicationSessionData {

	private final UserSessionData userSessionData;
	private final ManagedApplication managedApplication;
	private final OrganizationFieldView organizationFieldView;
	private final MobileApplicationNavigation mobileNavigation;

	private final LoadedApplication mainApplication;
	private final ResponsiveApplication responsiveApplication;
	private final SystemRegistry registry;
	private final ApplicationLocalizationProvider mainApplicationLocalizationProvider;

	public ManagedApplicationSessionData(UserSessionData userSessionData, ManagedApplication managedApplication, MobileApplicationNavigation mobileNavigation) {
		this.userSessionData = userSessionData;
		this.managedApplication = managedApplication;
		this.organizationFieldView = managedApplication.getOrganizationField() != null ? OrganizationFieldView.getById(managedApplication.getOrganizationField().getId()) : null;
		this.mobileNavigation = mobileNavigation;

		this.responsiveApplication = ResponsiveApplication.createApplication(
				SessionContext.current().getClientInfo().isMobileDevice() ?
						new MobileAssembler(mobileNavigation, userSessionData.getDictionary()) :
						new DesktopApplicationAssembler());
		registry = userSessionData.getRegistry();
		this.mainApplication = registry.getLoadedApplication(managedApplication.getMainApplication());
		this.mainApplicationLocalizationProvider = new UserLocalizationProvider(
				userSessionData.getSessionUser().getRankedLanguages(),
				registry.getDictionary(),
				registry.getSystemDictionary(),
				registry.getLoadedApplication(managedApplication.getMainApplication()).getApplicationLocalizationProvider()
		);
	}

	public PerspectiveSessionData createPerspectiveSessionData(ManagedApplicationPerspective managedApplicationPerspective) {
		LoadedApplication loadedApplication = registry.getLoadedApplication(managedApplicationPerspective.getApplicationPerspective().getApplication());
		ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(managedApplicationPerspective.getApplicationPerspective().getApplication());
		ApplicationPrivilegeProvider privilegeProvider = userSessionData.getUserPrivileges().getApplicationPrivilegeProvider(PrivilegeApplicationKey.create(managedApplicationPerspective));
		PerspectiveBuilder perspectiveBuilder = loadedApplication.getPerspectiveBuilder(managedApplicationPerspective.getApplicationPerspective().getName());
		if (userSessionData.getUser().getUserAccountStatus() == UserAccountStatus.SUPER_ADMIN) {
			privilegeProvider = new AllowAllPrivilegeProvider();
		}
		return new PerspectiveSessionData(this, managedApplication, managedApplicationPerspective, perspectiveBuilder, privilegeProvider, localizationProvider, registry.getDocumentConverterSupplier());
	}

	public ApplicationInstanceData getUnmanagedApplicationData() {
		LoadedApplication loadedApplication = registry.getLoadedApplication(managedApplication.getMainApplication());
		ApplicationPrivilegeProvider privilegeProvider = userSessionData.getUserPrivileges().getApplicationPrivilegeProvider(PrivilegeApplicationKey.createUnmanagedKey(managedApplication));
		if (userSessionData.getUser().getUserAccountStatus() == UserAccountStatus.SUPER_ADMIN) {
			privilegeProvider = new AllowAllPrivilegeProvider();
		}
		return new UnmanagedApplicationSessionData(userSessionData, managedApplication, loadedApplication, responsiveApplication, privilegeProvider, mainApplicationLocalizationProvider, registry.getDocumentConverterSupplier());
	}


	public boolean isUnmanagedApplication() {
		return registry.getLoadedApplication(managedApplication.getMainApplication()).isUnmanagedPerspectives();
	}

	public ApplicationLocalizationProvider getMainApplicationLocalizationProvider() {
		return mainApplicationLocalizationProvider;
	}

	public UserSessionData getUserSessionData() {
		return userSessionData;
	}

	public ManagedApplication getManagedApplication() {
		return managedApplication;
	}

	public MobileApplicationNavigation getMobileNavigation() {
		return mobileNavigation;
	}

	public OrganizationFieldView getOrganizationFieldView() {
		return organizationFieldView;
	}

	public LoadedApplication getMainApplication() {
		return mainApplication;
	}

	public ResponsiveApplication getResponsiveApplication() {
		return responsiveApplication;
	}
}
