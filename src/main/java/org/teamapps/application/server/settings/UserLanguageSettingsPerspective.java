package org.teamapps.application.server.settings;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.Button;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.template.BaseTemplateRecord;

public class UserLanguageSettingsPerspective extends AbstractApplicationPerspective {

	public UserLanguageSettingsPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		Perspective perspective = getPerspective();
		View view = perspective.addView(View.createView(StandardLayout.CENTER, ApplicationIcons.DICTIONARY, getLocalized(Dictionary.LANGUAGE), null));
		view.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		User user = User.getById(getApplicationInstanceData().getUser().getId());
		ComboBox<Language> languageComboBox = Language.createComboBox(getApplicationInstanceData());

		Language language = Language.getLanguageByIsoCode(getApplicationInstanceData().getUser().getLanguage());
		languageComboBox.setValue(language);

		Button<BaseTemplateRecord> saveButton = Button.create(ApplicationIcons.OK, getLocalized(Dictionary.SAVE));

		ResponsiveForm<?> form = new ResponsiveForm<>(100, 150, 250);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);

		formLayout.addSection(null, null).setDrawHeaderLine(false).setCollapsible(false);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.LANGUAGE), languageComboBox);
		formLayout.addLabelAndField(saveButton);

		view.setComponent(form);


		saveButton.onClicked.addListener(() -> {
			Language lang = languageComboBox.getValue();
			if (lang != null) {
				user.setLanguages(lang.getIsoCode()).save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
			}
		});

	}
}
