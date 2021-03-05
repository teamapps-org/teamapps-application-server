package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.ApplicationInfoDataElement;
import org.teamapps.application.server.system.utils.IconUtils;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationVersion;
import org.teamapps.model.controlcenter.ApplicationVersionData;
import org.teamapps.universaldb.index.text.TextFilter;

public class ApplicationArtifactInstallationPhase implements ApplicationInstallationPhase {

	@Override
	public void checkApplication(ApplicationInfo applicationInfo) {
		try {
			if (!applicationInfo.getErrors().isEmpty()) {
				return;
			}
			if (applicationInfo.getApplicationJar() != null) {
				String binaryHash = applicationInfo.getBinaryHash();
				ApplicationVersion applicationVersion = ApplicationVersion.filter().binaryHash(TextFilter.textEqualsFilter(binaryHash)).executeExpectSingleton();
				if (applicationVersion != null) {
					applicationInfo.addError("This jar is already installed: " + applicationVersion.getApplication().getName() + ": " + applicationVersion.getVersion());
					return;
				}
			}
			ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
			String applicationName = applicationBuilder.getApplicationName();
			if (
					applicationName == null ||
							applicationName.isEmpty() ||
							applicationName.contains(" ") ||
							applicationName.contains(".")) {
				applicationInfo.addError("Invalid application name: " + applicationName);
				return;
			}
			org.teamapps.application.api.versioning.ApplicationVersion applicationVersion = applicationBuilder.getApplicationVersion();
			if (applicationVersion == null) {
				applicationInfo.addError("Missing application version");
				return;
			}
			if (applicationBuilder.getApplicationTitleKey() == null) {
				applicationInfo.addError("Missing application title");
				return;
			}
			String versionString = applicationVersion.getVersion();
			applicationInfo.setName(applicationName);
			applicationInfo.setReleaseNotes(applicationInfo.getApplicationBuilder().getReleaseNotes());
			applicationInfo.setVersion(versionString);
			Application application = applicationInfo.getApplication();
			if (application != null) {
				for (ApplicationVersion version : application.getVersions()) {
					String installedVersion = version.getVersion();
					if (installedVersion.equals(versionString)) {
						applicationInfo.addWarning("This version is already installed: " + versionString);
						applicationInfo.setApplicationVersion(version);
					}
					if (installedVersion.compareTo(versionString) > 0) {
						applicationInfo.addWarning("A higher version is already installed! This version: " + versionString + ", installed Version: " + installedVersion);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking artifact:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {
		Application application = applicationInfo.getApplication();
		ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
		if (application == null) {
			application = Application.create()
					.setName(applicationInfo.getName());
		}
		application
				.setIcon(IconUtils.encodeNoStyle(applicationBuilder.getApplicationIcon()))
				.setTitleKey(applicationBuilder.getApplicationTitleKey())
				.setDescriptionKey(applicationBuilder.getApplicationDescriptionKey())
				.setUnmanagedApplication(applicationInfo.isUnmanagedPerspectives())
				.save();
		ApplicationVersion applicationVersion = ApplicationVersion.create()
				.setApplication(application)
				.setVersion(applicationInfo.getVersion())
				.setBinary(applicationInfo.getApplicationJar())
				.setBinaryHash(applicationInfo.getBinaryHash())
				.setReleaseNotes(applicationInfo.getReleaseNotes())
				.setDataModelData(createVersionData(applicationInfo.getDataModelData()))
				.setLocalizationData(createVersionData(applicationInfo.getLocalizationData()))
				.setPrivilegeData(createVersionData(applicationInfo.getPrivilegeData()))
				.setPerspectiveData(createVersionData(applicationInfo.getPerspectiveData()))
				.save();
		applicationInfo.setApplicationVersion(applicationVersion);
	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {

	}

	private ApplicationVersionData createVersionData(ApplicationInfoDataElement dataElement) {
		if (dataElement == null) {
			return null;
		}
		return ApplicationVersionData
				.create()
				.setData(dataElement.getData())
				.setDataRows(dataElement.getData() == null ? 0 : dataElement.getData().split("\n").length)
				.setDataAdded(String.join("\n", dataElement.getDataAdded()))
				.setDataAddedRows(dataElement.getDataAdded().size())
				.setDataRemoved(String.join("\n", dataElement.getDataRemoved()))
				.setDataRemovedRows(dataElement.getDataRemoved().size())
				.save();
	}
}
