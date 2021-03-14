package org.teamapps.application.server.system.template;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.session.SessionContext;

import java.util.HashMap;
import java.util.Map;

public class PropertyProviders {

	public static PropertyProvider<Application> createApplicationPropertyProvider(UserSessionData userSessionData) {
		return (application, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(application);
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(application.getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(application.getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(application.getDescriptionKey()));
			return map;
		};
	}

	public static PropertyProvider<ApplicationPerspective> createApplicationPerspectivePropertyProvider(UserSessionData userSessionData) {
		return (applicationPerspective, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(applicationPerspective.getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(applicationPerspective.getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(applicationPerspective.getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(applicationPerspective.getDescriptionKey()));
			return map;
		};
	}

	public static PropertyProvider<ApplicationPrivilege> createApplicationPrivilegePropertyProvider(UserSessionData userSessionData) {
		return (applicationPrivilege, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(applicationPrivilege.getPrivilegeGroup().getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(applicationPrivilege.getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(applicationPrivilege.getTitleKey()));
			return map;
		};
	}

	public static PropertyProvider<ApplicationPrivilegeGroup> createApplicationPrivilegeGroupPropertyProvider(UserSessionData userSessionData) {
		return (applicationPrivilegeGroup, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(applicationPrivilegeGroup.getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(applicationPrivilegeGroup.getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(applicationPrivilegeGroup.getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(applicationPrivilegeGroup.getDescriptionKey()));
			return map;
		};
	}

	public static PropertyProvider<ApplicationVersion> createSimpleApplicationVersionPropertyProvider() {
		return (version, propertyNames) -> {
			SessionContext context = SessionContext.current();
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, context.getIconProvider().decodeIcon(version.getApplication().getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, version.getVersion());
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, version.getReleaseNotes());
			return map;
		};
	}

	public static PropertyProvider<ApplicationVersion> createApplicationVersionPropertyProvider(UserSessionData userSessionData) {
		return (version, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(version.getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(version.getApplication().getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(version.getApplication().getTitleKey()) + ": " + version.getVersion());
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, version.getReleaseNotes());
			return map;
		};
	}

	public static PropertyProvider<ManagedApplication> createManagedApplicationPropertyProvider(UserSessionData userSessionData) {
		return (managedApplication, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(managedApplication.getMainApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, managedApplication.getIcon() != null ? userSessionData.decodeIcon(managedApplication.getIcon()) : userSessionData.decodeIcon(managedApplication.getMainApplication().getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, managedApplication.getTitleKey() != null ? localizationProvider.getLocalized(managedApplication.getTitleKey()) : localizationProvider.getLocalized(managedApplication.getMainApplication().getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, managedApplication.getDescriptionKey() != null ? localizationProvider.getLocalized(managedApplication.getDescriptionKey()) : localizationProvider.getLocalized(managedApplication.getMainApplication().getDescriptionKey()));
			map.put(BaseTemplate.PROPERTY_BADGE, managedApplication.getHidden() ? localizationProvider.getLocalized(Dictionary.HIDDEN) : null);
			return map;
		};
	}

	public static PropertyProvider<ManagedApplicationPerspective> createManagedApplicationPerspectivePropertyProvider(UserSessionData userSessionData) {
		return (managedApplicationPerspective, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(managedApplicationPerspective.getApplicationPerspective().getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, managedApplicationPerspective.getIconOverride() != null ? userSessionData.decodeIcon(managedApplicationPerspective.getIconOverride()): userSessionData.decodeIcon(managedApplicationPerspective.getApplicationPerspective().getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, managedApplicationPerspective.getTitleKeyOverride() != null ? localizationProvider.getLocalized(managedApplicationPerspective.getTitleKeyOverride()) : localizationProvider.getLocalized(managedApplicationPerspective.getApplicationPerspective().getTitleKey()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, managedApplicationPerspective.getDescriptionKeyOverride() != null ? localizationProvider.getLocalized(managedApplicationPerspective.getDescriptionKeyOverride()) : localizationProvider.getLocalized(managedApplicationPerspective.getApplicationPerspective().getDescriptionKey()));
			return map;
		};
	}

	public static PropertyProvider<ManagedApplicationGroup> createManagedApplicationGroupPropertyProvider(UserSessionData userSessionData) {
		return (managedApplicationGroup, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getDictionary();
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(managedApplicationGroup.getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(managedApplicationGroup.getTitleKey()));
			return map;
		};
	}

	public static PropertyProvider<OrganizationField> createOrganizationFieldPropertyProvider(ApplicationInstanceData applicationInstanceData) {
		return (orgField, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, orgField.getIcon() != null ? SessionContext.current().getIconProvider().decodeIcon(orgField.getIcon()) : null);
			map.put(BaseTemplate.PROPERTY_CAPTION, applicationInstanceData.getLocalized(orgField.getTitle()));
			return map;
		};
	}

	public static PropertyProvider<User> createUserPropertyProvider() {
		return (user, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			if (user.getProfilePicture() != null) {
				map.put(BaseTemplate.PROPERTY_IMAGE, SessionContext.current().createFileLink(user.getProfilePicture().retrieveFile())); //todo optimize: access should be cached!
			} else {
				map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.USER);
			}
			map.put(BaseTemplate.PROPERTY_CAPTION, user.getLastName() + ", " + user.getFirstName());
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, null); //user.getContainer() != null ? user.getContainer().getOrganizationUnit().getName().getText() :
			return map;
		};
	}

	public static PropertyProvider<String> createStringPropertyProvider(Icon icon) {
		return (s, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, icon);
			map.put(BaseTemplate.PROPERTY_CAPTION, s);
			return map;
		};
	}
}
