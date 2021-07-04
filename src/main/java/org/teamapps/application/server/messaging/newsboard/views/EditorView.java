package org.teamapps.application.server.messaging.newsboard.views;

import org.teamapps.application.api.application.AbstractApplicationView;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.event.Event;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.field.richtext.RichTextEditor;
import org.teamapps.ux.component.field.richtext.ToolbarVisibilityMode;

public class EditorView extends AbstractApplicationView {

	public Event<String> onTextUpdate = new Event<>();
	private RichTextEditor editor;

	public EditorView(ApplicationInstanceData applicationInstanceData) {
		super(applicationInstanceData);
		editor = new RichTextEditor();
		editor.setLocale(getUser().getLocale());
		editor.setToolbarVisibilityMode(ToolbarVisibilityMode.VISIBLE);
		editor.setCssStyle(".UiRichTextEditor", "height", "100%");

		editor.onTextInput.addListener(text -> onTextUpdate.fire(text));

//		editor.setUploadedFileToUrlConverter(uploadedFile -> {
//			File file = uploadedFile.getAsFile();
//
//		});
	}

	public Component getComponent() {
		return editor;
	}

	public void showMessage(String messageHtml) {
		editor.setValue(messageHtml);
	}

	public String getValue() {
		return editor.getValue();
	}
}
