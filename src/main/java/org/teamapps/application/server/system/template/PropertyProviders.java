package org.teamapps.application.server.system.template;

import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.model.controlcenter.*;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.utils.IconUtils;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.template.BaseTemplate;

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

	public static PropertyProvider<ApplicationVersion> createApplicationVersionPropertyProvider(UserSessionData userSessionData) {
		return (version, propertyNames) -> {
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(version.getApplication());
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, userSessionData.decodeIcon(version.getApplication().getIcon()));
			map.put(BaseTemplate.PROPERTY_CAPTION, localizationProvider.getLocalized(version.getApplication().getTitleKey()) + ": " + localizationProvider.getLocalized(version.getVersion()));
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, localizationProvider.getLocalized(version.getReleaseNotes()));
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
	

}
