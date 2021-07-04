package org.teamapps.application.server.messaging.newsboard;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.messaging.MessagingPrivileges;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;

public class NewsBoardPerspectiveBuilder extends AbstractPerspectiveBuilder {

	public NewsBoardPerspectiveBuilder() {
		super("newsBoardPerspective", ApplicationIcons.MESSAGE, "newsBoard.title", "newsBoard.desc");
	}

	@Override
	public boolean autoProvisionPerspective() {
		return true;
	}

	@Override
	public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider privilegeProvider) {
		return privilegeProvider.isAllowed(MessagingPrivileges.LAUNCH_PERSPECTIVE_NEWS_BOARD);
	}

	@Override
	public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		return new NewsBoardPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
	}
}
