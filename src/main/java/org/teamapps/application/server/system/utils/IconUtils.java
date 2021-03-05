package org.teamapps.application.server.system.utils;

import org.teamapps.icons.Icon;
import org.teamapps.icons.IconLibraryRegistry;
import org.teamapps.icons.IconProvider;
import org.teamapps.icons.SessionIconProvider;
import org.teamapps.ux.session.SessionContext;

public class IconUtils {

	private static SessionIconProvider NO_STYLE_ICON_PROVIDER = new SessionIconProvider(new IconProvider(new IconLibraryRegistry()));


	public static String encodeNoStyle(Icon icon) {
		if (icon == null) {
			return null;
		}
		return NO_STYLE_ICON_PROVIDER.encodeIcon(icon, false);
	}

	public static Icon<?, ?> decodeIconNoStyles(String name) {
		if (name == null) {
			return null;
		}
		try {
			return NO_STYLE_ICON_PROVIDER.decodeIcon(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Icon<?, ?> decodeIcon(String name) {
		if (name == null) {
			return null;
		}
		try {
			return SessionContext.current().getIconProvider().decodeIcon(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
