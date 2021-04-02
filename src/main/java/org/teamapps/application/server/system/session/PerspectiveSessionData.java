package org.teamapps.application.server.system.session;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.PerspectiveBuilder;
import org.teamapps.application.api.desktop.ApplicationDesktop;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.privilege.*;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.reporting.convert.DocumentConverter;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.component.progress.MultiProgressDisplay;

import java.util.List;
import java.util.function.Supplier;

public class PerspectiveSessionData implements ApplicationInstanceData {

	private final ManagedApplicationSessionData managedApplicationSessionData;
	private final ManagedApplication managedApplication;
	private final ManagedApplicationPerspective managedApplicationPerspective;
	private final PerspectiveBuilder perspectiveBuilder;
	private final ApplicationPrivilegeProvider privilegeProvider;
	private final ApplicationLocalizationProvider localizationProvider;
	private final Supplier<DocumentConverter> documentConverterSupplier;
	private final UserSessionData userSessionData;

	public PerspectiveSessionData(ManagedApplicationSessionData managedApplicationSessionData, ManagedApplication managedApplication, ManagedApplicationPerspective managedApplicationPerspective, PerspectiveBuilder perspectiveBuilder, ApplicationPrivilegeProvider privilegeProvider, ApplicationLocalizationProvider localizationProvider, Supplier<DocumentConverter> documentConverterSupplier) {
		this.managedApplicationSessionData = managedApplicationSessionData;
		this.managedApplication = managedApplication;
		this.managedApplicationPerspective = managedApplicationPerspective;
		this.perspectiveBuilder = perspectiveBuilder;
		this.privilegeProvider = privilegeProvider;
		this.localizationProvider = localizationProvider;
		this.documentConverterSupplier = documentConverterSupplier;
		this.userSessionData = managedApplicationSessionData.getUserSessionData();
	}

	public Icon getIcon() {
		if (managedApplicationPerspective.getIconOverride() != null) {
			return IconUtils.decodeIcon(managedApplicationPerspective.getIconOverride());
		} else {
			return perspectiveBuilder.getIcon();
		}
	}

	public String getTitle() {
		if (managedApplicationPerspective.getTitleKeyOverride() != null) {
			return userSessionData.getDictionary().getLocalized(managedApplicationPerspective.getTitleKeyOverride());
		} else {
			return localizationProvider.getLocalized(perspectiveBuilder.getTitleKey());
		}
	}

	public String getDescription() {
		if (managedApplicationPerspective.getDescriptionKeyOverride() != null) {
			return userSessionData.getDictionary().getLocalized(managedApplicationPerspective.getDescriptionKeyOverride());
		} else {
			return localizationProvider.getLocalized(perspectiveBuilder.getDescriptionKey());
		}
	}

	public ManagedApplicationPerspective getManagedApplicationPerspective() {
		return managedApplicationPerspective;
	}

	public ManagedApplicationSessionData getManagedApplicationSessionData() {
		return managedApplicationSessionData;
	}

	public PerspectiveBuilder getPerspectiveBuilder() {
		return perspectiveBuilder;
	}

	@Override
	public SessionUser getUser() {
		return managedApplicationSessionData.getUserSessionData().getSessionUser();
	}

	@Override
	public OrganizationFieldView getOrganizationField() {
		return managedApplicationSessionData.getOrganizationFieldView();
	}

	@Override
	public int getManagedApplicationId() {
		return managedApplication.getId();
	}

	@Override
	public DocumentConverter getDocumentConverter() {
		if (documentConverterSupplier == null) {
			return null;
		} else {
			return documentConverterSupplier.get();
		}
	}

	@Override
	public MultiProgressDisplay getMultiProgressDisplay() {
		return managedApplicationSessionData.getResponsiveApplication().getMultiProgressDisplay();
	}

	@Override
	public void showPerspective(Perspective perspective) {
		managedApplicationSessionData.getResponsiveApplication().showPerspective(perspective);
	}

	@Override
	public ApplicationDesktop createApplicationDesktop(Icon icon, String title, boolean select, boolean closable) {
		//todo ...
		return null;
	}

	@Override
	public void writeActivityLog(String title, String data) {
		SystemLog.create()
				.setManagedApplication(managedApplication)
				.setManagedPerspective(managedApplicationPerspective)
				.setApplication(managedApplicationPerspective.getApplicationPerspective().getApplication())
				.setLogLevel(LogLevel.INFO)
				.setMessage(title)
				.setDetails(data)
				.save();
	}

	@Override
	public void writeExceptionLog(String title, Throwable throwable) {
		SystemLog.create()
				.setManagedApplication(managedApplication)
				.setManagedPerspective(managedApplicationPerspective)
				.setApplication(managedApplicationPerspective.getApplicationPerspective().getApplication())
				.setLogLevel(LogLevel.ERROR)
				.setMessage(title)
				.setDetails(ExceptionUtils.getStackTrace(throwable))
				.save();
	}

	@Override
	public String getLocalized(String s, Object... objects) {
		return localizationProvider.getLocalized(s, objects);
	}

	@Override
	public String getLocalized(TranslatableText translatableText) {
		return localizationProvider.getLocalized(translatableText);
	}

	@Override
	public boolean isAllowed(SimplePrivilege simplePrivilege) {
		return privilegeProvider.isAllowed(simplePrivilege);
	}

	@Override
	public boolean isAllowed(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege, OrganizationUnitView organizationUnitView) {
		return privilegeProvider.isAllowed(simpleOrganizationalPrivilege, organizationUnitView);
	}

	@Override
	public boolean isAllowed(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege, PrivilegeObject privilegeObject) {
		return privilegeProvider.isAllowed(simpleCustomObjectPrivilege, privilegeObject);
	}

	@Override
	public boolean isAllowed(StandardPrivilegeGroup standardPrivilegeGroup, Privilege privilege) {
		return privilegeProvider.isAllowed(standardPrivilegeGroup, privilege);
	}

	@Override
	public boolean isAllowed(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege, OrganizationUnitView organizationUnitView) {
		return privilegeProvider.isAllowed(organizationalPrivilegeGroup, privilege, organizationUnitView);
	}

	@Override
	public boolean isAllowed(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege, PrivilegeObject privilegeObject) {
		return privilegeProvider.isAllowed(customObjectPrivilegeGroup, privilege, privilegeObject);
	}

	@Override
	public List<OrganizationUnitView> getAllowedUnits(SimpleOrganizationalPrivilege simpleOrganizationalPrivilege) {
		return privilegeProvider.getAllowedUnits(simpleOrganizationalPrivilege);
	}

	@Override
	public List<OrganizationUnitView> getAllowedUnits(OrganizationalPrivilegeGroup organizationalPrivilegeGroup, Privilege privilege) {
		return privilegeProvider.getAllowedUnits(organizationalPrivilegeGroup, privilege);
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(SimpleCustomObjectPrivilege simpleCustomObjectPrivilege) {
		return privilegeProvider.getAllowedPrivilegeObjects(simpleCustomObjectPrivilege);
	}

	@Override
	public List<PrivilegeObject> getAllowedPrivilegeObjects(CustomObjectPrivilegeGroup customObjectPrivilegeGroup, Privilege privilege) {
		return privilegeProvider.getAllowedPrivilegeObjects(customObjectPrivilegeGroup, privilege);
	}
}
