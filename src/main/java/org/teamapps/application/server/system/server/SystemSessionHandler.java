package org.teamapps.application.server.system.server;

import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.ux.session.SessionContext;

public interface SystemSessionHandler {

	void handleNewSession(SessionContext context);

	void handleAuthenticatedUser(UserSessionData userSessionDat, SessionContext context);

	void handleLogout(UserSessionData userSessionDat, SessionContext context);
}
