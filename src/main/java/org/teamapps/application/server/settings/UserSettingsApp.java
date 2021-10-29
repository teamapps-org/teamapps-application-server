package org.teamapps.application.server.settings;

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
import org.teamapps.universaldb.schema.SchemaInfoProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserSettingsApp extends AbstractApplicationBuilder {

	public UserSettingsApp() {
		super("userSettings", ApplicationIcons.WINDOW_GEAR, "userSettings.title", "userSettings.desc");
	}

	@Override
	public List<PerspectiveBuilder> getPerspectiveBuilders() {
		return Arrays.asList(
				new UserLanguageSettingsPerspectiveBuilder()
		);
	}

	@Override
	public ApplicationVersion getApplicationVersion() {
		return ApplicationVersion.create(0, 1);
	}

	@Override
	public List<ApplicationRole> getApplicationRoles() {
		return null;
	}

	@Override
	public List<PrivilegeGroup> getPrivilegeGroups() {
		return Collections.emptyList();
	}

	@Override
	public LocalizationData getLocalizationData() {
		return LocalizationData.createFromPropertyFiles("org.teamapps.application.server.i18n.userSettings", getClass().getClassLoader(),
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
						Locale.forLanguageTag("sv"),
						Locale.forLanguageTag("fa"),
						Locale.forLanguageTag("iw"), //he
						Locale.forLanguageTag("hi"),
						Locale.forLanguageTag("hr"),
						Locale.forLanguageTag("in"), //id
						Locale.forLanguageTag("ko"),
						Locale.forLanguageTag("mk"),
						Locale.forLanguageTag("mn"),
						Locale.forLanguageTag("sr"),
						Locale.forLanguageTag("tr"),
						Locale.forLanguageTag("vi")
				));
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
		return true;
	}
}
