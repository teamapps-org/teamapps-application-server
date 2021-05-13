package org.teamapps.application.server.chat;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;

public class ChatPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public ChatPerspectiveBuilder() {
		super("chatPerspective", ApplicationIcons.MESSAGES, "chat.title", "chat.title");
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return true;
	}

	@Override
	public boolean autoProvisionPerspective() {
		return true;
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		return new ChatPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
	}
}
