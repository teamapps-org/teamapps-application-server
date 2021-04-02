/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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
