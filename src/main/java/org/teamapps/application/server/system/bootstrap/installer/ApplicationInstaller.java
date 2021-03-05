package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.config.LocalizationConfig;
import org.teamapps.application.server.system.localization.LocalizationUtil;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationVersion;
import org.teamapps.universaldb.UniversalDB;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ApplicationInstaller {

	private final ApplicationInfo applicationInfo;
	private final UniversalDB universalDB;
	private final TranslationService translationService;

	private final List<ApplicationInstallationPhase> applicationInstallationPhases;


	public static ApplicationInstaller createJarInstaller(File applicationJar, UniversalDB universalDB, TranslationService translationService, LocalizationConfig localizationConfig) {
		return new ApplicationInstaller(new ApplicationInfo(applicationJar), universalDB, translationService, localizationConfig);
	}

	public static ApplicationInstaller createClassInstaller(ApplicationBuilder applicationBuilder, UniversalDB universalDB, TranslationService translationService, LocalizationConfig localizationConfig) {
		return new ApplicationInstaller(new ApplicationInfo(applicationBuilder), universalDB, translationService, localizationConfig);
	}

	private ApplicationInstaller(ApplicationInfo applicationInfo, UniversalDB universalDB, TranslationService translationService, LocalizationConfig localizationConfig) {
		this.applicationInfo = applicationInfo;
		this.universalDB = universalDB;
		this.translationService = translationService;
		applicationInstallationPhases = Arrays.asList(
				new ApplicationJarInstallationPhase(),
				new ApplicationArtifactInstallationPhase(),
				new DataModelInstallationPhase(universalDB),
				new LocalizationDataInstallationPhase(localizationConfig),
				new PrivilegeDataInstallationPhase(),
				new PerspectiveDataInstallationPhase()
		);
	}

	public boolean isInstalled() {
		if (!applicationInfo.isChecked()) {
			checkApplication();
		}
		Application application = applicationInfo.getApplication();
		if (application == null) {
			return false;
		}
		String version = applicationInfo.getApplicationBuilder().getApplicationVersion().getVersion();
		ApplicationVersion matchingVersion = application.getVersions().stream().filter(v -> v.getVersion().equals(version)).findFirst().orElse(null);
		return matchingVersion != null;
	}

	public ApplicationInfo checkApplication() {
		applicationInstallationPhases.forEach(phase -> phase.checkApplication(applicationInfo));
		applicationInfo.setChecked(true);
		return applicationInfo;
	}

	public boolean installApplication() {
		if (applicationInfo.isChecked() && applicationInfo.getErrors().isEmpty()) {
			applicationInstallationPhases.forEach(phase -> phase.installApplication(applicationInfo));
			applicationInfo.getApplicationBuilder().bootstrapApplicationBuilder();
			applicationInfo.getApplication().setInstalledVersion(applicationInfo.getApplicationVersion()).save();
			LocalizationUtil.translateAllApplicationValues(translationService, applicationInfo.getApplication());
			return true;
		} else {
			return false;
		}
	}

	public LoadedApplication loadApplication() {
		if (applicationInfo.isChecked() && applicationInfo.getErrors().isEmpty()) {
			applicationInfo.createLoadedApplication();
			applicationInstallationPhases.forEach(phase -> phase.loadApplication(applicationInfo));
			return applicationInfo.getLoadedApplication();
		}
		return null;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}
}
