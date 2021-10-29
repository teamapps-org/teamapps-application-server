package org.teamapps.application.server.controlcenter.database;

public class DbExplorerUtils {

	public static String getFirstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String createTitleFromCamelCase(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (i < 3) {
				sb.append(c);
			} else {
				if (Character.isUpperCase(c)) {
					sb.append(" ");
				}
				sb.append(c);
			}
		}
		return getFirstUpper(sb.toString());
	}
}
