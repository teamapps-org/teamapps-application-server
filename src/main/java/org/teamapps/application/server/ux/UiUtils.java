package org.teamapps.application.server.ux;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.ux.localize.TranslatableField;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.session.SessionContext;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UiUtils {

	public static void showNotification(Icon icon, String title) {
		SessionContext.current().showNotification(icon, title);
	}

	public static void showSaveNotification(boolean success, ApplicationInstanceData applicationInstanceData) {
		SessionContext.current().showNotification(
				success ? ApplicationIcons.OK : ApplicationIcons.ERROR,
				success ? applicationInstanceData.getLocalized(Dictionary.RECORD_SUCCESSFULLY_SAVED) : applicationInstanceData.getLocalized(Dictionary.ERROR_WHEN_SAVING)
		);
	}

	public static <TYPE> TemplateField<TYPE> createTemplateField(Template template, PropertyProvider<TYPE> propertyProvider) {
		TemplateField<TYPE> templateField = new TemplateField<>(template);
		templateField.setPropertyProvider(propertyProvider);
		return templateField;
	}

	public static TemplateField<Boolean> createBooleanTemplateField(Icon trueIcon, String trueTitle, Icon falseIcon, String falseTitle) {
		TemplateField<Boolean> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyExtractor((value, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> value ? trueIcon : falseIcon;
			case BaseTemplate.PROPERTY_CAPTION -> value ? trueTitle : falseTitle;
			default -> null;
		});
		return templateField;
	}

	public static TemplateField<String> createSingleValueTemplateField(Icon icon, String title) {
		TemplateField<String> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyExtractor((value, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> icon;
			case BaseTemplate.PROPERTY_CAPTION -> title;
			default -> null;
		});
		templateField.setValue(title);
		return templateField;
	}

	public static TextField createSingleValueTextField(String value) {
		TextField textField = new TextField();
		textField.setValue(value);
		textField.setEditingMode(FieldEditingMode.READONLY);
		return textField;
	}

	public static DisplayField createSingleValueDisplayField(String html) {
		DisplayField displayField = new DisplayField(false, true);
		displayField.setValue(html);
		return displayField;
	}

	public static TagComboBox<String> createSingleValueTagComboBox(Icon icon, List<String> values) {
		TagComboBox<String> tagComboBox = new TagComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		tagComboBox.setValue(values);
		tagComboBox.setPropertyExtractor((s, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> icon;
			case BaseTemplate.PROPERTY_CAPTION -> s;
			default -> null;
		});
		tagComboBox.setEditingMode(FieldEditingMode.READONLY);
		return tagComboBox;
	}

	public static <TYPE> TagComboBox<TYPE> createTagComboBox(Template template, PropertyProvider<TYPE> propertyProvider) {
		TagComboBox<TYPE> tagComboBox = new TagComboBox<>(template);
		tagComboBox.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		tagComboBox.setPropertyProvider(propertyProvider);
		return tagComboBox;
	}

	public static <RECORD> Function<RECORD, String> createRecordQueryStringFunction(PropertyProvider<RECORD> propertyProvider, String... properties) {
		Set<String> keys = properties != null && properties.length > 0 ? new HashSet<>(Arrays.asList(properties)) : new HashSet<>(Arrays.asList(BaseTemplate.PROPERTY_CAPTION)) ;
		return record -> {
			Map<String, Object> values = propertyProvider.getValues(record, keys);
			return values.entrySet().stream()
					.filter(entry -> keys.contains(entry.getKey()))
					.map(Map.Entry::getValue)
					.filter(Objects::nonNull)
					.map(v -> (String) v)
					.collect(Collectors.joining(", ")).toLowerCase();
		};
	}

	public static <RECORD> Function<RECORD, String> createRecordToStringFunction(PropertyProvider<RECORD> propertyProvider) {
		return createRecordToStringFunction(propertyProvider, BaseTemplate.PROPERTY_CAPTION);
	}

	public static <RECORD> Function<RECORD, String> createRecordToStringFunction(PropertyProvider<RECORD> propertyProvider, String property) {
		return record -> {
			Object value = propertyProvider.getValues(record, Collections.singleton(property)).get(property);
			return value != null ? (String) value : "";
		};
	}

	public static <RECORD> BiFunction<RECORD, String, Boolean> createRecordFilterFunction(PropertyProvider<RECORD> propertyProvider, String... properties) {
		Function<RECORD, String> queryStringFunction = createRecordQueryStringFunction(propertyProvider, properties);
		return (record, s) -> s == null || s.isBlank() || queryStringFunction.apply(record).contains(s.toLowerCase());
	}

}
