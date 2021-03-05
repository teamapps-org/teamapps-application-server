package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.server.system.bootstrap.ApplicationInfo;

public interface ApplicationInstallationPhase {

	void checkApplication(ApplicationInfo applicationInfo);

	void installApplication(ApplicationInfo applicationInfo);

	void loadApplication(ApplicationInfo applicationInfo);

}
