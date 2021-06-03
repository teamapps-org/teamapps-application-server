package org.teamapps.application.server.ui.dialogue;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.ux.component.dialogue.FormDialogue;
import org.teamapps.ux.component.field.upload.FileFieldDisplayType;
import org.teamapps.ux.component.field.upload.simple.FileItem;
import org.teamapps.ux.component.field.upload.simple.SimpleFileField;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class UploadDialogue {

	public static void createFileUploadDialogue(Consumer<File> fileConsumer, ApplicationInstanceData applicationInstanceData) {
		FormDialogue formDialogue =new FormDialogue(ApplicationIcons.UPLOAD, applicationInstanceData.getLocalized(Dictionary.UPLOAD), applicationInstanceData.getLocalized(Dictionary.UPLOAD));
		SimpleFileField fileField =new SimpleFileField();
		formDialogue.addField(null, applicationInstanceData.getLocalized(Dictionary.UPLOAD), fileField);
		fileField.setDisplayType(FileFieldDisplayType.FLOATING);
		formDialogue.setCloseable(true);
		formDialogue.setCloseOnEscape(true);
		formDialogue.addOkCancelButtons(applicationInstanceData.getLocalized(Dictionary.O_K), applicationInstanceData.getLocalized(Dictionary.CANCEL));
		formDialogue.onOk.addListener(() -> {
			List<FileItem> files = fileField.getValue();
			if (files != null && !files.isEmpty()) {
				FileItem fileItem = files.get(0);
				File file = fileItem.getFile();
				formDialogue.close();
				fileConsumer.accept(file);
			}
		});
		formDialogue.show();
	}



}
