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

import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.model.ComboBoxModel;

import java.util.*;
import java.util.function.BiFunction;
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
		Function<RECORD, String> recordToStringFunction = UiUtils.createRecordToStringFunction(propertyProvider);
		comboBox.setRecordToStringFunction(recordToStringFunction);
		comboBox.setModel(query -> query == null || query.isBlank() ? records.get() : records.get().stream().filter(record -> recordToStringFunction.apply(record).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList()));
		return comboBox;
	}

	public static <RECORD> ComboBoxModel<RECORD> createComboBoxModel(Supplier<List<RECORD>> recordSupplier, PropertyProvider<RECORD> propertyProvider, int limit, String... properties) {
		BiFunction<RECORD, String, Boolean> recordFilterFunction = UiUtils.createRecordFilterFunction(propertyProvider, properties);
		return query -> {
			List<RECORD> records = recordSupplier.get();
			return records != null ? records.stream().filter(record -> recordFilterFunction.apply(record, query)).limit(limit).collect(Collectors.toList()) : Collections.emptyList();
		};
	}

}
