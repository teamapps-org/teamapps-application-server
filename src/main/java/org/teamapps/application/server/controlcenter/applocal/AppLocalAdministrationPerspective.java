package org.teamapps.application.server.controlcenter.applocal;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.controlcenter.applications.ApplicationUpdatesPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.roles.RolesPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.roles.UserRoleAssignmentPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.systemlog.SystemLogPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.systenconfig.ApplicationConfigurationPerspectiveBuilder;
import org.teamapps.application.server.controlcenter.translations.TranslationsPerspectiveBuilder;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.application.PerspectiveMenuPanel;
import org.teamapps.databinding.MutableValue;

public class AppLocalAdministrationPerspective extends AbstractManagedApplicationPerspective {

	private PerspectiveMenuPanel menuPanel;
	private TranslationsPerspectiveBuilder translationsPerspectiveBuilder;

	public AppLocalAdministrationPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		translationsPerspectiveBuilder = new TranslationsPerspectiveBuilder();
		menuPanel = PerspectiveMenuPanel.createMenuPanel(getApplicationInstanceData(),
				translationsPerspectiveBuilder,
				new UserRoleAssignmentPerspectiveBuilder(),
				new RolesPerspectiveBuilder(),
				new ApplicationUpdatesPerspectiveBuilder(),
				new SystemLogPerspectiveBuilder(),
				new ApplicationConfigurationPerspectiveBuilder()
		);
		setPerspectiveMenuPanel(menuPanel.getComponent());
	}

	@Override
	public void handleOnAfterLoad() {
		menuPanel.openPerspective(translationsPerspectiveBuilder);
	}
}
