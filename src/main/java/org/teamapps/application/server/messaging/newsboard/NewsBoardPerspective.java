package org.teamapps.application.server.messaging.newsboard;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.messaging.newsboard.views.MessageView;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.model.controlcenter.NewsBoardMessage;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.Button;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplateRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsBoardPerspective extends AbstractManagedApplicationPerspective {

	private View masterView;
	private final UserSessionData userSessionData;


	public NewsBoardPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.MESSAGE, getLocalized("newsBoard.title"), null));
		masterView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.1f));

		updateMessages();

		Button<BaseTemplateRecord> addButton = Button.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD));
		masterView.getPanel().setRightHeaderField(addButton);
		addButton.onClicked.addListener(() -> showMessageWindow(null));
	}

	public void updateMessages() {
		updateMessages(getUser().getRankedLanguages());
	}

	public void updateMessages(List<String> rankedLanguages) {
		List<NewsBoardMessage> messages = isAppFilter() ? NewsBoardMessage.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : NewsBoardMessage.getAll();
		messages = new ArrayList<>(messages);
		ComboBox<String> languageComboBox = NewsBoardUtils.createLanguageSelectionComboBox(NewsBoardUtils.getUsedLanguageValues(messages), getApplicationInstanceData());
		masterView.getPanel().setLeftHeaderField(languageComboBox);
		Collections.reverse(messages);
		MessageView messageView = new MessageView(messages, getApplicationInstanceData(), rankedLanguages, this::showMessageWindow);
		masterView.setComponent(messageView.getComponent());
		languageComboBox.setValue(rankedLanguages.get(0));
		languageComboBox.onValueChanged.addListener(language -> {
			if (NewsBoardUtils.USER_LANGUAGES.equals(language)) {
				updateMessages();
			} else {
				updateMessages(Collections.singletonList(language));
			}
		});
	}


	private void showMessageWindow(NewsBoardMessage message) {
		if (message == null) {
			message = NewsBoardMessage.create()
					.setLanguage(getUser().getLocale().getLanguage())
					.setMetaCreatedBy(getUser().getId())
					.setOrganizationField(getOrganizationField());
		}
		new MessageWindow(message, this, getApplicationInstanceData(), userSessionData.getRegistry());
	}




	/*
	Message Window:
		Toolbar:
			Preview/Edit: show editor / show result
			Show original: (only if this is not the original) show original message
			Translations: show translation panel
			Publish without translation
			Publish with translations: tag combo with pre selected languages
			Add translation: select not yet translated languages (from the translatable list)
			Hide: only if already published
			Delete:
			Images: show image panel

		Translation panel:
			Table: original + list of translations
			On click: show translation in message view

		Message panel:
			Read mode: show full message with all pictures & background
			Edit mode: show editor

		Images:
			Toolbar: Add, delete, up, down: on add-> show upload dialogue (filter jpg) -> resize thumb, resize if > 1
			Table: thumb, name, size

		Message headers:
			italic: -This is a machine translated message and may contain inaccuracies.
			Orange border: -This message is not yet published


		Newsboard -> Welcome, Icon: message

		Model:
			NewsBoardMessage
				published
				htmlMessage
				language
				images
				translations
				organizationField
				organizationUnit

			NewsBoardMessageImage
				file
				fileName
				position

			NewsBoardMessageTranslation
				message
				language
				translation

	 */
}
