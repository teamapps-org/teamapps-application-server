package org.teamapps.application.server.messaging.newsboard.views;

import org.teamapps.model.controlcenter.NewsBoardMessage;

public interface ViewMessageHandler {

	void handleViewMessageRequest(NewsBoardMessage message);
}
