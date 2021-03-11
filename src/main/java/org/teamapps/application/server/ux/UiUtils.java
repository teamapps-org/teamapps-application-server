package org.teamapps.application.server.ux;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.ux.translatable.TranslatableField;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.component.toolbar.Toolbar;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.window.Window;
import org.teamapps.ux.session.SessionContext;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UiUtils {

	public static void showSaveNotification(boolean success, ApplicationInstanceData applicationInstanceData) {
		SessionContext.current().showNotification(
				success ? ApplicationIcons.OK : ApplicationIcons.ERROR,
				success ? applicationInstanceData.getLocalized(Dictionary.RECORD_SUCCESSFULLY_SAVED) : applicationInstanceData.getLocalized(Dictionary.ERROR_WHEN_SAVING)
		);
	}

	public static TranslatableField createTranslatableField(ApplicationInstanceData applicationInstanceData) {
		return new TranslatableField(applicationInstanceData);
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


}
