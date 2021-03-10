package org.teamapps.application.server.ux;

import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.model.ComboBoxModel;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicationUiUtils {


	public static ComboBox<Application> createApplicationComboBox(Template template, UserSessionData userSessionData) {
		ComboBox<Application> comboBox = new ComboBox<>(template);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		PropertyProvider<Application> propertyProvider = PropertyProviders.createApplicationPropertyProvider(userSessionData);
		comboBox.setPropertyProvider(propertyProvider);
		Function<Application, String> recordToStringFunction = application -> (String) propertyProvider.getValues(application, null).get(BaseTemplate.PROPERTY_CAPTION);
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(query -> query == null || query.isBlank() ? Application.getAll() : Application.getAll().stream().filter(app -> recordToStringFunction.apply(app).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));
		return comboBox;
	}
}
