package org.teamapps.application.server.system.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.application.server.controlcenter.ControlCenterAppBuilder;
import org.teamapps.application.server.controlcenter.dbexplorer.DatabaseExplorerAppBuilder;
import org.teamapps.application.server.system.bootstrap.installer.ApplicationInstaller;
import org.teamapps.application.server.system.config.SystemConfig;
import org.teamapps.application.server.system.logging.DatabaseLogAppender;
import org.teamapps.application.server.system.login.LoginHandler;
import org.teamapps.application.server.system.machinetranslation.MachineTranslation;
import org.teamapps.application.server.system.passwordhash.SecurePasswordHash;
import org.teamapps.application.server.system.server.ApplicationServer;
import org.teamapps.application.server.system.server.SessionHandler;
import org.teamapps.application.server.system.server.SessionManager;
import org.teamapps.application.server.system.template.Templates;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.icon.flags.FlagIcon;
import org.teamapps.icon.standard.StandardIcon;
import org.teamapps.icon.standard.StandardIconStyles;
import org.teamapps.model.ControlCenterSchema;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationVersion;
import org.teamapps.model.controlcenter.User;
import org.teamapps.model.controlcenter.UserAccountStatus;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.index.file.FileValue;
import org.teamapps.ux.session.SessionContext;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BootstrapSessionHandler implements SessionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private SessionManager sessionManager;
	private UniversalDB universalDB;
	private File configPath;
	private SystemRegistry systemRegistry;


	private static Class standardIconClass;

	static {
		try {
			standardIconClass = Class.forName("org.teamapps.icon.standard.StandardIcon");
		} catch (Exception var1) {
		}
	}

	public void installNewSystem(File applicationJar) throws Exception {
		sessionManager.updateSessionHandler(applicationJar);
	}

	@Override
	public void init(SessionManager sessionManager, UniversalDB universalDB, File configPath) {
		try {
			this.sessionManager = sessionManager;
			this.universalDB = universalDB;
			this.configPath = configPath;
			startSystem();
		} catch (Exception e) {
			LOGGER.error("Error initializing system:", e);
		}
	}

	private void startSystem() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		ControlCenterSchema schema = new ControlCenterSchema();
		universalDB.addAuxiliaryModel(schema, classLoader);
		universalDB.installAuxiliaryModelClassed(schema, classLoader);
		DatabaseLogAppender.startLogger();

		SystemConfig systemConfig = new SystemConfig();
		MachineTranslation machineTranslation = null;
		if (systemConfig.getMachineTranslation().isActive()) {
			machineTranslation = new MachineTranslation();
			machineTranslation.setGoogleTranslationKey(systemConfig.getMachineTranslation().getGoogleKey());
			machineTranslation.setDeepLKey(systemConfig.getMachineTranslation().getDeepLKey());
		}
		systemRegistry = new SystemRegistry(systemConfig, this, universalDB, machineTranslation);

		systemRegistry.installAndLoadApplication(new ControlCenterAppBuilder());
		systemRegistry.installAndLoadApplication(new DatabaseExplorerAppBuilder(universalDB));

		for (Application application : Application.getAll()) {
			ApplicationVersion installedVersion = application.getInstalledVersion();
			FileValue binary = installedVersion.getBinary();
			if (binary != null) {
				File jarFile = binary.getFileSupplier().get();
				ApplicationInstaller jarInstaller = ApplicationInstaller.createJarInstaller(jarFile, universalDB, systemRegistry.getTranslationService(), systemRegistry.getSystemConfig().getLocalizationConfig());
				if (jarInstaller.isInstalled()) {
					systemRegistry.loadApplication(jarInstaller);
				}
			}
		}

	}


	@Override
	public void handleSessionStart(SessionContext context) {
		if (standardIconClass != null) {
			context.getIconProvider().registerIconLibrary(standardIconClass);
		}
		//todo remove:
		context.getIconProvider().setDefaultStyleForIconClass(StandardIcon.class, StandardIconStyles.VIVID_STANDARD_SHADOW_1);

		context.getIconProvider().registerIconLibrary(FlagIcon.class);
		context.registerTemplates(Arrays.stream(Templates.values())
				.collect(Collectors.toMap(Enum::name, Templates::getTemplate)));

		new LoginHandler(systemRegistry).handleNewSession(context);
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			LOGGER.error("Error missing path!");
			return;
		}
		File path = new File(args[0]);
		ApplicationServer applicationServer = new ApplicationServer(path);
		applicationServer.setSessionHandler(new BootstrapSessionHandler());
		applicationServer.start();
		if (User.getCount() == 0) {
			User.create()
					.setFirstName("Super")
					.setLastName("Admin")
					.setLogin("admin")
					.setPassword(SecurePasswordHash.createDefault().createSecureHash("teamapps!"))
					.setUserAccountStatus(UserAccountStatus.SUPER_ADMIN)
					.setLanguages(ValueConverterUtils.compressStringList(Arrays.asList("de", "en", "fr")))
					.save();
		}
	}
}
