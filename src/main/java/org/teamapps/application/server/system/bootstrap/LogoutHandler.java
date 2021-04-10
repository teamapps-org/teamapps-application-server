package org.teamapps.application.server.system.bootstrap;

import org.teamapps.ux.session.SessionContext;

public interface LogoutHandler {

	void handleLogout(SessionContext context);
}
