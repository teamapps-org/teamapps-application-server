package org.teamapps.application.server.ux.form;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.ux.window.ApplicationWindow;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.field.AbstractField;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;

import java.util.ArrayList;
import java.util.List;

public class FormWindow extends ApplicationWindow {

	private ResponsiveForm responsiveForm;
	private ResponsiveFormLayout formLayout;
	private List<AbstractField> fields = new ArrayList<>();

	public FormWindow(Icon icon, String title, ApplicationInstanceData applicationInstanceData) {
		super(icon, title, applicationInstanceData);
		init();
	}

	private void init() {
		responsiveForm = new ResponsiveForm<>(100, 150, 0);
		formLayout = responsiveForm.addResponsiveFormLayout(500);
		setContent(responsiveForm);
	}

	public void addSection() {
		formLayout.addSection().setDrawHeaderLine(false);
	}

	public void addSection(Icon icon, String title) {
		formLayout.addSection(icon, title);
	}

	public void addField(String label, Component field) {
		formLayout.addLabelAndComponent(null, label, field);
		if (field instanceof AbstractField) {
			fields.add((AbstractField) field);
		}
	}

	public ResponsiveForm getResponsiveForm() {
		return responsiveForm;
	}

	public ResponsiveFormLayout getFormLayout() {
		return formLayout;
	}
}
