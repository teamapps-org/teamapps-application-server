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

public class UiUtils {

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

	public static Window createWindow(Icon icon, String title) {
		int width = (int) Math.max(800, Math.min(1400, SessionContext.current().getClientInfo().getViewPortWidth() * 0.85f));
		int height = (int) Math.max(600, Math.min(900, SessionContext.current().getClientInfo().getScreenHeight() * 0.85f));
		Window window = new Window(icon, title, width, height, null);
		window.setToolbar(new Toolbar());
		window.setCloseable(true);
		window.setCloseOnEscape(true);
		window.setMaximizable(true);
		return window;
	}

	public static void addCancelCloseButton(Window window, ApplicationInstanceData applicationInstanceData) {
		Toolbar toolbar = window.getToolbar();
		if (toolbar == null) {
			toolbar = new Toolbar();
			window.setToolbar(toolbar);
		}
		ToolbarButtonGroup buttonGroup = toolbar.addButtonGroup(new ToolbarButtonGroup());
		ToolbarButton closeButton = ToolbarButton.create(ApplicationIcons.ERROR, applicationInstanceData.getLocalized(Dictionary.CANCEL), applicationInstanceData.getLocalized(Dictionary.CANCEL_AND_CLOSE_WINDOW));
		closeButton.onClick.addListener((Runnable) window::close);
		buttonGroup.addButton(closeButton);

	}
}
