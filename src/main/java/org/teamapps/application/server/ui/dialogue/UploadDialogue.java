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
