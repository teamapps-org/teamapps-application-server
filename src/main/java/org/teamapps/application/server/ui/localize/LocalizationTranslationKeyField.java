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
package org.teamapps.application.server.ui.localize;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.ui.TranslationKeyField;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
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

	private final SystemRegistry systemRegistry;
	private final ComboBox<String> localizationKeyCombo;
	private final TextField keyTextField;
	private final LinkButton linkButton;

	public LocalizationTranslationKeyField(String linkButtonCaption, ApplicationInstanceData applicationInstanceData, SystemRegistry systemRegistry, Supplier<Application> applicationSupplier) {
		this.systemRegistry = systemRegistry;
		localizationKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, applicationInstanceData, applicationSupplier);
		localizationKeyCombo.setDropDownTemplate(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		localizationKeyCombo.setShowClearButton(true);
		keyTextField = new TextField();
		keyTextField.setEditingMode(FieldEditingMode.READONLY);

		linkButton = new LinkButton(linkButtonCaption);
		localizationKeyCombo.onValueChanged.addListener(keyTextField::setValue);
		LocalizationKeyWindow localizationKeyWindow = new LocalizationKeyWindow(applicationInstanceData, systemRegistry, applicationSupplier);
		linkButton.onClicked.addListener(() -> {
			localizationKeyWindow.onNewKey.addListener(value -> {
				localizationKeyCombo.setValue(value);
				keyTextField.setValue(value);
			});
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
