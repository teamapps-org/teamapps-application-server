package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.AbstractApplicationPerspective;
import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.dummy.DummyComponent;

public class Perspective2 extends AbstractPerspectiveBuilder {

	public Perspective2() {
		super("p2", ApplicationIcons.ADDRESS_BOOK, "p.title2", "p.desc2");
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
				getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.USERS, "View title 2", new DummyComponent()));
			}
		};
	}
}
