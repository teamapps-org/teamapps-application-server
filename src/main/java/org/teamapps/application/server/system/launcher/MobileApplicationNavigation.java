package org.teamapps.application.server.system.launcher;

import org.teamapps.event.Event;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;

public class MobileApplicationNavigation implements MobileNavigation {

	public Event<Void> onBackOperation = new Event<>();
	private Event<Void> onShowStartViewRequest = new Event<>();
	private Component applicationLauncher;
	private View applicationMenu;
	private boolean backOperationAvailable;

	public void setApplicationLauncher(Component applicationLauncher) {
		this.applicationLauncher = applicationLauncher;
	}

	public void setApplicationMenu(View applicationMenu) {
		this.applicationMenu = applicationMenu;
	}

	@Override
	public Event<Void> onShowStartViewRequest() {
		return onShowStartViewRequest;
	}

	@Override
	public Component getApplicationLauncher() {
		return applicationLauncher;
	}

	@Override
	public View getApplicationMenuView() {
		return applicationMenu;
	}

	@Override
	public boolean isBackOperationAvailable() {
		return backOperationAvailable;
	}

	@Override
	public void fireBackOperation() {
		backOperationAvailable = false;
		onBackOperation.fire();
	}

	public void setBackOperationAvailable(boolean backOperationAvailable) {
		this.backOperationAvailable = backOperationAvailable;
	}
}
