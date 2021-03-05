package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.AbstractApplicationPerspective;
import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.dummy.DummyComponent;
import org.teamapps.ux.component.panel.Panel;

public class Perspective1 extends AbstractPerspectiveBuilder {

	public Perspective1() {
		super("p1", ApplicationIcons.FIRST_AID_BOX, "p.title1", "p.desc1");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider applicationPrivilegeProvider) {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> mutableValue) {

		return new AbstractApplicationPerspective(applicationInstanceData, mutableValue) {
			@Override
			public void createUi() {
				getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.BRAIN, "View title 1", new DummyComponent()));
				setPerspectiveMenuPanel(new Panel(ApplicationIcons.LASER_POINTER, "Sub menu!!!"));
			}
		};
	}
}
