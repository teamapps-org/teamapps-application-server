package org.teamapps.application.server.ui.localize;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.ui.TranslationKeyField;
import org.teamapps.application.server.system.localization.SystemLocalizationProvider;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.ux.component.field.AbstractField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.linkbutton.LinkButton;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.function.Supplier;

public class LocalizationTranslationKeyField implements TranslationKeyField {

	private final ComboBox<String> localizationKeyCombo;
	private final TextField keyTextField;
	private final LinkButton linkButton;

	public LocalizationTranslationKeyField(String linkButtonCaption, ApplicationInstanceData applicationInstanceData, Supplier<Application> applicationSupplier) {
		this(linkButtonCaption, applicationInstanceData, applicationSupplier, null);
	}

	public LocalizationTranslationKeyField(String linkButtonCaption, ApplicationInstanceData applicationInstanceData, SystemLocalizationProvider systemLocalizationProvider) {
		this(linkButtonCaption, applicationInstanceData, null, systemLocalizationProvider);
	}

	private LocalizationTranslationKeyField(String linkButtonCaption, ApplicationInstanceData applicationInstanceData, Supplier<Application> applicationSupplier, SystemLocalizationProvider systemLocalizationProvider) {
		localizationKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, applicationInstanceData, applicationSupplier);
		localizationKeyCombo.setDropDownTemplate(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		localizationKeyCombo.setShowClearButton(true);
		keyTextField = new TextField();
		keyTextField.setEditingMode(FieldEditingMode.READONLY);

		linkButton = new LinkButton(linkButtonCaption);
		localizationKeyCombo.onValueChanged.addListener(keyTextField::setValue);
		LocalizationKeyWindow localizationKeyWindow = applicationSupplier != null ? LocalizationKeyWindow.createApplicationKey(applicationSupplier.get(), applicationInstanceData) : LocalizationKeyWindow.createSystemKey(systemLocalizationProvider, applicationInstanceData);
		linkButton.onClicked.addListener(() -> {
			localizationKeyWindow.onNewKey.addListener(localizationKeyCombo::setValue);
			localizationKeyWindow.onNewKey.addListener(keyTextField::setValue);
			localizationKeyWindow.show();
		});
	}

	@Override
	public AbstractField<String> getSelectionField() {
		return localizationKeyCombo;
	}

	@Override
	public AbstractField<String> getKeyDisplayField() {
		return keyTextField;
	}

	@Override
	public LinkButton getKeyLinkButton() {
		return linkButton;
	}

	@Override
	public void setKey(String key) {
		localizationKeyCombo.setValue(key);
		keyTextField.setValue(key);
	}

	@Override
	public String getKey() {
		return localizationKeyCombo.getValue();
	}


}
