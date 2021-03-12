package org.teamapps.application.server.controlcenter.translations;

import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.Icon;

public enum TranslationMode {

	TRANSLATE,
	PROOFREAD,
	ADMINISTRATE,

	;
	public Icon getIcon() {
		return switch (this) {
			case TRANSLATE -> ApplicationIcons.COPY;
			case PROOFREAD -> ApplicationIcons.CHECKS;
			case ADMINISTRATE -> ApplicationIcons.KEYS;
		};
	}

	public String getTranslationKey() {
		return switch (this) {
			case TRANSLATE -> "translations.mode.translate";
			case PROOFREAD -> "translations.mode.proofread";
			case ADMINISTRATE -> "translations.mode.administrate";
		};
	}
}
