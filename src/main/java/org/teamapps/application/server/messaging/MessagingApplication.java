package org.teamapps.application.server.messaging;

import edu.emory.mathcs.backport.java.util.Collections;
import org.teamapps.application.api.application.AbstractApplicationBuilder;
import org.teamapps.application.api.application.perspective.PerspectiveBuilder;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.localization.LocalizationLanguages;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.application.server.messaging.newsboard.NewsBoardPerspectiveBuilder;
import org.teamapps.universaldb.schema.SchemaInfoProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MessagingApplication extends AbstractApplicationBuilder {


	public MessagingApplication() {
		super("messagingApp", ApplicationIcons.MAIL, "messaging.title", "messaging.desc");
	}

	@Override
	public List<PerspectiveBuilder> getPerspectiveBuilders() {
		return Arrays.asList(
				new NewsBoardPerspectiveBuilder()
		);
	}

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(0,5);
	}

	@Override
	public List<ApplicationRole> getApplicationRoles() {
		return Collections.emptyList();
	}

	@Override
	public List<PrivilegeGroup> getPrivilegeGroups() {
		return Collections.emptyList();
	}

	@Override
	public LocalizationData getLocalizationData() {
		return LocalizationData.createFromPropertyFiles("org.teamapps.application.server.i18n.messagingApp", getClass().getClassLoader(),
				new LocalizationLanguages(
						Locale.ENGLISH
				).setMachineTranslatedLanguages(
						Locale.GERMAN,
						Locale.FRENCH,
						Locale.ITALIAN,
						Locale.JAPANESE,
						Locale.CHINESE,
						Locale.forLanguageTag("bg"),
						Locale.forLanguageTag("cs"),
						Locale.forLanguageTag("da"),
						Locale.forLanguageTag("el"),
						Locale.forLanguageTag("es"),
						Locale.forLanguageTag("et"),
						Locale.forLanguageTag("fi"),
						Locale.forLanguageTag("hu"),
						Locale.forLanguageTag("lt"),
						Locale.forLanguageTag("lv"),
						Locale.forLanguageTag("nl"),
						Locale.forLanguageTag("pl"),
						Locale.forLanguageTag("pt"),
						Locale.forLanguageTag("ro"),
						Locale.forLanguageTag("ru"),
						Locale.forLanguageTag("sk"),
						Locale.forLanguageTag("sl"),
						Locale.forLanguageTag("sv")
				)
		);
	}
	@Override
	public SchemaInfoProvider getDatabaseModel() {
		return null;
	}

	@Override
	public ApplicationConfig getApplicationConfig() {
		return null;
	}

	@Override
	public boolean isApplicationAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return privilegeProvider.isAllowed(MessagingPrivileges.LAUNCH_APPLICATION);
	}
}
