package org.teamapps.application.server.controlcenter.dbexplorer;

import org.teamapps.application.api.application.AbstractApplication;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.udb.decider.DeciderSet;
import org.teamapps.udb.explorer.DatabaseExplorerApp;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.ux.application.ResponsiveApplication;
import org.teamapps.ux.component.Component;

public class DatabaseExplorerApplication extends AbstractApplication {


	private final UniversalDB universalDB;
	private final DatabaseExplorerApp explorerApp;

	public DatabaseExplorerApplication(ResponsiveApplication responsiveApplication, ApplicationInstanceData applicationInstanceData, UniversalDB universalDB) {
		super(responsiveApplication, applicationInstanceData);
		this.universalDB = universalDB;
		explorerApp = new DatabaseExplorerApp(universalDB, responsiveApplication, entity -> DeciderSet.createReadOnly());
	}

	@Override
	public Component getUi() {
		return explorerApp.getApplication().getUi();
	}
}
