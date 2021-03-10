package org.teamapps.application.server.ux.form;

import org.teamapps.icons.Icon;
import org.teamapps.ux.component.panel.Panel;

public class FormUtils {


	public static Panel createFormPanel(Icon icon, String title) {
		Panel panel = new Panel(icon, title);
		panel.setHideTitleBar(true);
		return panel;
	}
}
