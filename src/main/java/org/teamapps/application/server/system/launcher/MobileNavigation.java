package org.teamapps.application.server.system.launcher;

import org.teamapps.event.Event;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;

public interface MobileNavigation {

	Event<Void> onShowStartViewRequest();

	Component getApplicationLauncher();

	View getApplicationMenuView();

	boolean isBackOperationAvailable();

	void fireBackOperation();
}
