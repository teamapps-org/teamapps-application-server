package org.teamapps.application.server.ux.combo;

import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComboBoxUtils {

	public static <RECORD> ComboBox<RECORD> createRecordComboBox(List<RECORD> records, PropertyProvider<RECORD> propertyProvider, Template template) {
		ComboBox<RECORD> comboBox = new ComboBox<>(template);
		comboBox.setPropertyProvider(propertyProvider);
		Function<RECORD, String> recordToStringFunction = record -> (String) propertyProvider.getValues(record, null).get(BaseTemplate.PROPERTY_CAPTION);
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(query -> query == null || query.isBlank() ? records : records.stream().filter(record -> recordToStringFunction.apply(record).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));
		return comboBox;
	}
}
