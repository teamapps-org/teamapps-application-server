package org.teamapps.application.server.system.auth;

import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.session.SessionContext;

import java.util.Map;

public interface AuthenticationHandler {

	User authenticate(SessionContext context, Map<String, Object> clientParameters);
}
