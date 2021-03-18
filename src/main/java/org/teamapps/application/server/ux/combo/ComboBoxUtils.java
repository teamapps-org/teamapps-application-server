package org.teamapps.application.server.ux.combo;

import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.model.ComboBoxModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ComboBoxUtils {

	public static <RECORD> ComboBox<RECORD> createComboBox(ComboBoxModel<RECORD> comboBoxModel,PropertyProvider<RECORD> propertyProvider, Template template) {
		ComboBox<RECORD> comboBox = new ComboBox<>(template);
		comboBox.setPropertyProvider(propertyProvider);
		Function<RECORD, String> recordToStringFunction = record -> {
			Map<String, Object> values = propertyProvider.getValues(record, Collections.singleton(BaseTemplate.PROPERTY_CAPTION));
			Object result = values.get(BaseTemplate.PROPERTY_CAPTION);
			return (String) result;
		};
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(comboBoxModel);
		return comboBox;
	}

	public static <RECORD> ComboBox<RECORD> createRecordComboBox(List<RECORD> records, PropertyProvider<RECORD> propertyProvider, Template template) {
		return createRecordComboBox(() -> records, propertyProvider, template);
	}

	public static <RECORD> ComboBox<RECORD> createRecordComboBox(Supplier<List<RECORD>> records, PropertyProvider<RECORD> propertyProvider, Template template) {
		ComboBox<RECORD> comboBox = new ComboBox<>(template);
		comboBox.setPropertyProvider(propertyProvider);
		Function<RECORD, String> recordToStringFunction = record -> {
			Map<String, Object> values = propertyProvider.getValues(record, Collections.singleton(BaseTemplate.PROPERTY_CAPTION));
			Object result = values.get(BaseTemplate.PROPERTY_CAPTION);
			return (String) result;
		};
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(query -> query == null || query.isBlank() ? records.get() : records.get().stream().filter(record -> recordToStringFunction.apply(record).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));
		return comboBox;
	}
}
