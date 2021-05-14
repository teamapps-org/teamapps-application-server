/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.system.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.application.api.application.BaseApplicationBuilder;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.bootstrap.installer.ApplicationInstaller;
import org.teamapps.application.server.system.config.DocumentConversionConfig;
import org.teamapps.application.server.system.config.MachineTranslationConfig;
import org.teamapps.application.server.system.config.SystemConfig;
import org.teamapps.application.server.system.localization.DictionaryLocalizationProvider;
import org.teamapps.application.server.system.localization.GlobalLocalizationProvider;
import org.teamapps.application.server.system.localization.LocalizationUtil;
import org.teamapps.application.server.system.localization.SystemLocalizationProvider;
import org.teamapps.application.server.system.machinetranslation.MachineTranslation;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.application.server.system.server.SessionRegistryHandler;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.model.controlcenter.*;
import org.teamapps.reporting.convert.DocumentConverter;
import org.teamapps.universaldb.UniversalDB;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SystemRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final BootstrapSessionHandler bootstrapSessionHandler;
	private final UniversalDB universalDB;
	private final ApplicationConfig<SystemConfig> applicationConfig;
	private TranslationService translationService;
	private final DictionaryLocalizationProvider dictionary;
	private final SystemLocalizationProvider systemDictionary;
	private final GlobalLocalizationProvider globalLocalizationProvider;
	private final Map<Application, LoadedApplication> loadedApplicationMap = new HashMap<>();
	private final ManagedApplicationGroup unspecifiedApplicationGroup;
	private final BaseResourceLinkProvider baseResourceLinkProvider;
	private SessionRegistryHandler sessionRegistryHandler;
	private DocumentConverter documentConverter;


	public SystemRegistry(BootstrapSessionHandler bootstrapSessionHandler, UniversalDB universalDB, ApplicationConfig<SystemConfig> applicationConfig) {
		SystemConfig systemConfig = applicationConfig.getConfig();
		this.bootstrapSessionHandler = bootstrapSessionHandler;
		this.universalDB = universalDB;
		this.applicationConfig = applicationConfig;
		this.dictionary = new DictionaryLocalizationProvider(translationService, systemConfig.getLocalizationConfig().getRequiredLanguages());
		this.systemDictionary = new SystemLocalizationProvider();
		this.globalLocalizationProvider = new GlobalLocalizationProvider(this);
		this.baseResourceLinkProvider = new BaseResourceLinkProvider();
		this.unspecifiedApplicationGroup = getOrCreateUnspecifiedApplicationGroup();
		handleConfigUpdate();
		applicationConfig.onConfigUpdate.addListener(this::handleConfigUpdate);
	}

	private void handleConfigUpdate() {
		SystemConfig config = applicationConfig.getConfig();
		DocumentConversionConfig documentConversionConfig = config.getDocumentConversionConfig();
		if (documentConversionConfig.isActive()) {
			documentConverter = DocumentConverter.createRemoteConverter(documentConversionConfig.getHost(), documentConversionConfig.getUser(), documentConversionConfig.getPassword());
		}
		MachineTranslationConfig machineTranslationConfig = config.getMachineTranslationConfig();
		if (machineTranslationConfig.isActive()) {
			MachineTranslation machineTranslation = new MachineTranslation();
			machineTranslation.setGoogleTranslationKey(machineTranslationConfig.getGoogleKey());
			machineTranslation.setDeepLKey(machineTranslationConfig.getDeepLKey(), machineTranslationConfig.isDeepLFreeApi());
			this.translationService = machineTranslation;
		}
	}

	public void updateGlobalLocalizationProvider() {
		globalLocalizationProvider.updateLocalizationData();
		systemDictionary.update();
	}

	public void machineTranslateMissingEntries() {
		if (translationService != null) {
			LocalizationUtil.translateAllValues(translationService);
		}
	}

	private ManagedApplicationGroup getOrCreateUnspecifiedApplicationGroup() {
		if (ManagedApplicationGroup.getCount() == 0) {
			return ManagedApplicationGroup.create().setIcon(IconUtils.encodeNoStyle(ApplicationIcons.HOME)).setTitleKey(Dictionary.APPLICATIONS).save();
		} else {
			return ManagedApplicationGroup.getAll().get(0);
		}
	}

	public ApplicationInstaller createJarInstaller(File jarFile) {
		return ApplicationInstaller.createJarInstaller(jarFile, universalDB, translationService, getSystemConfig().getLocalizationConfig());
	}

	public boolean installAndLoadApplication(BaseApplicationBuilder baseApplicationBuilder) {
		ApplicationInstaller applicationInstaller = ApplicationInstaller.createClassInstaller(baseApplicationBuilder, universalDB, translationService, getSystemConfig().getLocalizationConfig());
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
							.setSingleApplication(applicationInfo.isUnmanagedPerspectives())
							.save();
					List<ApplicationPerspective> perspectives = application.getPerspectives().stream().filter(ApplicationPerspective::getAutoProvision).collect(Collectors.toList());
					for (ApplicationPerspective perspective : perspectives) {
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
		try {
			LoadedApplication loadedApplication = applicationInstaller.loadApplication();
			System.out.println("Loaded app:" + applicationInstaller.getApplicationInfo().getName());
			if (applicationInstaller.getApplicationInfo().getErrors().isEmpty()) {
				addLoadedApplication(loadedApplication);
			}
		} catch (Throwable e) {
			LOGGER.error("Error loading application: " + applicationInstaller.getApplicationInfo(), e);
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
		return () -> documentConverter;
	}

	public SystemConfig getSystemConfig() {
		return (SystemConfig) applicationConfig.getConfig();
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

	public SessionRegistryHandler getSessionRegistryHandler() {
		return sessionRegistryHandler;
	}

	public void setSessionRegistryHandler(SessionRegistryHandler sessionRegistryHandler) {
		this.sessionRegistryHandler = sessionRegistryHandler;
	}
}
