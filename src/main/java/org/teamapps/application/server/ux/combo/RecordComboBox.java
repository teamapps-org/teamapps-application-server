package org.teamapps.application.server.ux.combo;

import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordComboBox<RECORD> extends ComboBox<RECORD> {

	private final PropertyProvider<RECORD> propertyProvider;
	private final Function<RECORD, String> recordToStringFunction;
	private final Function<RECORD, String> recordToStringSearchFunction;

	public RecordComboBox(PropertyProvider<RECORD> propertyProvider, Template template) {
		super(template);
		this.propertyProvider = propertyProvider;
		setPropertyProvider(propertyProvider);
		recordToStringFunction = record -> (String) propertyProvider.getValues(record, null).get(BaseTemplate.PROPERTY_CAPTION);
		recordToStringSearchFunction = record -> {
			String value = (String) propertyProvider.getValues(record, null).get(BaseTemplate.PROPERTY_CAPTION);
			String value2 = (String) propertyProvider.getValues(record, null).get(BaseTemplate.PROPERTY_DESCRIPTION);
			String value3 = (String) propertyProvider.getValues(record, null).get(BaseTemplate.PROPERTY_BADGE);
			return (value != null ? value : "") + (value2 != null ? value2 : "") + (value3 != null ? value3 : "");
		};
		setRecordToStringFunction(recordToStringFunction);
	}

	public void setRecords(List<RECORD> records) {
		setModel(query -> query == null || query.isBlank() ? records : records.stream().filter(record -> recordToStringSearchFunction.apply(record).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));
	}

}
