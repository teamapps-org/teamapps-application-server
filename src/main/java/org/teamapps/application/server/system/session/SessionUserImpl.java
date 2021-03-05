package org.teamapps.application.server.system.session;

import com.ibm.icu.util.ULocale;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionUserImpl implements SessionUser {

	private final User user;
	private final SessionContext context;
	private final List<String> rankedLanguages;

	public SessionUserImpl(User user, SessionContext context) {
		this.user = user;
		this.context = context;
		rankedLanguages = new ArrayList<>();
		init();
	}

	private void init() {
		List<String> languages = ValueConverterUtils.getNameList(user.getLanguages());
		if (languages == null || languages.isEmpty()) {
			languages = new ArrayList<>();
			languages.add(context.getLocale().getLanguage());
		}
		rankedLanguages.addAll(languages);
	}

	@Override
	public int getId() {
		return user.getId();
	}

	@Override
	public String getName(boolean lastNameFirst) {
		if (lastNameFirst) {
			return user.getLastName() + ", " + user.getFirstName();
		} else {
			return user.getFirstName() + " " + user.getLastName();
		}
	}

	@Override
	public String getProfilePictureLink() {
		return null; //todo - link must be the same vor all users?
	}

	@Override
	public String getLargeProfilePictureLink() {
		return null; //todo
	}

	@Override
	public SessionContext getSessionContext() {
		return context;
	}

	@Override
	public ULocale getULocale() {
		return null;
	}

	@Override
	public Locale getLocale() {
		return context.getLocale();
	}

	@Override
	public List<String> getRankedLanguages() {
		return rankedLanguages;
	}
}
