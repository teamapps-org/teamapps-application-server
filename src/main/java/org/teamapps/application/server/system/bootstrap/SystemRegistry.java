package org.teamapps.application.server.system.bootstrap;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.bootstrap.installer.ApplicationInstaller;
import org.teamapps.application.server.system.config.SystemConfig;
import org.teamapps.application.server.system.localization.DictionaryLocalizationProvider;
import org.teamapps.application.server.system.localization.SystemLocalizationProvider;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.application.server.system.server.SecureResourceHandler;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.model.controlcenter.*;
import org.teamapps.reporting.convert.DocumentConverter;
import org.teamapps.universaldb.UniversalDB;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SystemRegistry {

	private final SystemConfig systemConfig;
	private final BootstrapSessionHandler bootstrapSessionHandler;
	private final UniversalDB universalDB;
	private final TranslationService translationService;
	private final DictionaryLocalizationProvider dictionary;
	private final SystemLocalizationProvider systemDictionary;
	private final Map<Application, LoadedApplication> loadedApplicationMap = new HashMap<>();
	private final ManagedApplicationGroup unspecifiedApplicationGroup;
	private final BaseResourceLinkProvider baseResourceLinkProvider;

	public SystemRegistry(SystemConfig systemConfig, BootstrapSessionHandler bootstrapSessionHandler, UniversalDB universalDB, TranslationService translationService) {
		this.systemConfig = systemConfig;
		this.bootstrapSessionHandler = bootstrapSessionHandler;
		this.universalDB = universalDB;
		this.translationService = translationService;
		this.dictionary = new DictionaryLocalizationProvider(translationService, systemConfig.getLocalizationConfig().getRequiredLanguages());
		this.systemDictionary = new SystemLocalizationProvider(translationService, systemConfig.getLocalizationConfig().getRequiredLanguages());
		this.baseResourceLinkProvider = new BaseResourceLinkProvider();
		unspecifiedApplicationGroup = getOrCreateUnspecifiedApplicationGroup();
	}

	private ManagedApplicationGroup getOrCreateUnspecifiedApplicationGroup() {
		if (ManagedApplicationGroup.getCount() == 0) {
			return ManagedApplicationGroup.create().setIcon(IconUtils.encodeNoStyle(ApplicationIcons.HOME)).setTitleKey(Dictionary.APPLICATIONS).save();
		} else {
			return ManagedApplicationGroup.getAll().get(0);
		}
	}

	public ApplicationInstaller createJarInstaller(File jarFile) {
		return ApplicationInstaller.createJarInstaller(jarFile, universalDB, translationService, systemConfig.getLocalizationConfig());
	}

	public boolean installAndLoadApplication(ApplicationBuilder applicationBuilder) {
		ApplicationInstaller applicationInstaller = ApplicationInstaller.createClassInstaller(applicationBuilder, universalDB, translationService, systemConfig.getLocalizationConfig());
		return installAndLoadApplication(applicationInstaller);
	}

	public boolean installAndLoadApplication(ApplicationInstaller applicationInstaller) {
		if (!applicationInstaller.isInstalled()) {
			if (applicationInstaller.installApplication()) {
				ApplicationInfo applicationInfo = applicationInstaller.getApplicationInfo();
				Application application = applicationInfo.getApplication();
				if (application.getVersions().size() == 1) {
					ManagedApplication managedApplication = ManagedApplication.create()
							.setMainApplication(application)
							.setApplicationGroup(unspecifiedApplicationGroup)
							.save();
					for (ApplicationPerspective perspective : application.getPerspectives()) {
						ManagedApplicationPerspective.create()
								.setManagedApplication(managedApplication)
								.setApplicationPerspective(perspective)
								.save();
					}
				}
			} else {
				System.out.println("Error installing " + applicationInstaller.getApplicationInfo().getName() + ": " + applicationInstaller.getApplicationInfo().getErrorMessage());
				System.out.println("\tWarnings:" + applicationInstaller.getApplicationInfo().getWarningMessage());
				return false;
			}
		}
		loadApplication(applicationInstaller);
		return true;
	}

	public void loadApplication(ApplicationInstaller applicationInstaller) {
		LoadedApplication loadedApplication = applicationInstaller.loadApplication();
		System.out.println("Loaded app:" + applicationInstaller.getApplicationInfo().getName());
		if (applicationInstaller.getApplicationInfo().getErrors().isEmpty()) {
			addLoadedApplication(loadedApplication);
		}
	}

	public void addLoadedApplication(LoadedApplication loadedApplication) {
		loadedApplicationMap.put(loadedApplication.getApplication(), loadedApplication);
	}

	public LoadedApplication getLoadedApplication(Application application) {
		return loadedApplicationMap.get(application);
	}

	public DictionaryLocalizationProvider getDictionary() {
		return dictionary;
	}

	public SystemLocalizationProvider getSystemDictionary() {
		return systemDictionary;
	}

	public Supplier<DocumentConverter> getDocumentConverterSupplier() {
		return null;
	}

	public SystemConfig getSystemConfig() {
		return systemConfig;
	}

	public BootstrapSessionHandler getBootstrapSessionHandler() {
		return bootstrapSessionHandler;
	}

	public TranslationService getTranslationService() {
		return translationService;
	}

	public BaseResourceLinkProvider getBaseResourceLinkProvider() {
		return baseResourceLinkProvider;
	}
}
