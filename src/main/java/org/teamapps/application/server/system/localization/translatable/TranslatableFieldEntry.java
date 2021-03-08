package org.teamapps.application.server.system.localization.translatable;

import org.teamapps.application.api.localization.Language;
import org.teamapps.universaldb.index.translation.TranslatableText;

public class TranslatableFieldEntry extends TranslatableText {

	private String value;
	private final Language language;
	private final TranslatableText originalTranslatableText;

	public TranslatableFieldEntry(String value, Language language, TranslatableText originalTranslatableText) {
		this.value = value;
		this.language = language;
		this.originalTranslatableText = originalTranslatableText;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Language getLanguage() {
		return language;
	}

	public TranslatableText getOriginalTranslatableText() {
		return originalTranslatableText;
	}
}
