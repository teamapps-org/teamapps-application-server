package org.teamapps.application.server.chat;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.model.controlcenter.ChatMessage;
import org.teamapps.model.controlcenter.SystemLog;
import org.teamapps.model.controlcenter.User;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.absolutelayout.Length;
import org.teamapps.ux.component.field.MultiLineTextField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.datetime.LocalDateTimeField;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.timegraph.TimeGraph;
import org.teamapps.ux.resource.ByteArrayResource;

public class ChatPerspective extends AbstractManagedApplicationPerspective {


	private final TwoWayBindableValue<ChatMessage> selectedUser = TwoWayBindableValue.create();
	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;


	public ChatPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View timeLineView = getPerspective().addView(View.createView(StandardLayout.TOP, ApplicationIcons.FORM, getLocalized(Dictionary.TIMELINE), null));
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.MESSAGES, getLocalized("chat.title"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.MESSAGES, getLocalized("chat.title"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));


		EntityModelBuilder<ChatMessage> userModelBuilder = new EntityModelBuilder<>(() -> ChatMessage.filter(), getApplicationInstanceData());
		TimeGraph timeGraph = userModelBuilder.createTimeGraph(ChatMessage::getMetaCreationDateAsEpochMilli, SystemLog.FIELD_META_CREATION_DATE);
		userModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("chat.title"));
		userModelBuilder.attachSearchField(masterView);
		userModelBuilder.onSelectedRecordChanged.addListener(selectedUser::set);
		Table<ChatMessage> table = userModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(32);
		table.setStripedRows(false);
		userModelBuilder.updateModels();

		timeLineView.setComponent(timeGraph);

		TemplateField<User> userTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider(getApplicationInstanceData()));

		table.addColumn(new TableColumn<ChatMessage>(ChatMessage.FIELD_CHAT_CHANNEL, "Channel", new TextField()).setDefaultWidth(150));
		table.addColumn(new TableColumn<ChatMessage>(ChatMessage.FIELD_AUTHOR, "Author", userTableField).setDefaultWidth(180));
		table.addColumn(new TableColumn<ChatMessage>(ChatMessage.FIELD_META_CREATION_DATE, "Date", new InstantDateTimeField()).setDefaultWidth(200));
		table.addColumn(new TableColumn<ChatMessage>(ChatMessage.FIELD_MESSAGE, "Message", new TextField()).setDefaultWidth(350));

		table.setPropertyExtractor((message, propertyName) -> switch (propertyName) {
			case ChatMessage.FIELD_CHAT_CHANNEL -> message.getChatChannel().getTitle();
			case ChatMessage.FIELD_AUTHOR -> message.getAuthor();
			case ChatMessage.FIELD_META_CREATION_DATE -> message.getMetaCreationDate();
			case ChatMessage.FIELD_MESSAGE -> message.getMessage();
			default -> null;
		});
		masterView.setComponent(table);

		TextField channelTitle = new TextField();
		TemplateField<User> author = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider(getApplicationInstanceData()));
		InstantDateTimeField dateTimeField = new InstantDateTimeField();
		MultiLineTextField messageField = new MultiLineTextField();
		messageField.setCssStyle("height", Length.ofPixels(150).toCssString());

		ResponsiveForm<?> form = new ResponsiveForm<>(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, "Channel", channelTitle);
		formLayout.addLabelAndField(null, "Author", author);
		formLayout.addLabelAndField(null, "Date", dateTimeField);
		formLayout.addLabelAndField(null, "Message", messageField);

		detailView.setComponent(form);

		selectedUser.onChanged().addListener(message -> {
			channelTitle.setValue(message.getChatChannel().getTitle());
			author.setValue(message.getAuthor());
			dateTimeField.setValue(message.getMetaCreationDate());
			messageField.setValue(message.getMessage());



		});

	}
}
