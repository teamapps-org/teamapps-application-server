package org.teamapps.application.server.ux.translatable;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.machinetranslation.TranslationService;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslatableTextUtils {


	public static TemplateField<TranslatableText> createTranslatableTemplateField(ApplicationInstanceData applicationInstanceData) {
		TemplateField<TranslatableText> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyProvider(createPropertyProvider(applicationInstanceData));
		return templateField;
	}


	public static PropertyProvider<TranslatableText> createPropertyProvider(ApplicationInstanceData applicationInstanceData) {
		List<String> rankedLanguages = applicationInstanceData.getUser().getRankedLanguages();
		return (translatableText, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			Map<String, String> translationMap = translatableText.getTranslationMap();
			for (String languageIso : rankedLanguages) {
				String text = translationMap.get(languageIso);
				if (text != null) {
					Language language = Language.getLanguageByIsoCode(languageIso);
					if (language != null) {
						map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
						map.put(BaseTemplate.PROPERTY_CAPTION, text);
						map.put(BaseTemplate.PROPERTY_DESCRIPTION, language.getIsoCode() + " (" + language.getLanguageLocalized(applicationInstanceData) + ")");
						return map;
					}
				}
			}
			if (translatableText.getText() != null) {
				Language language = Language.getLanguageByIsoCode(translatableText.getOriginalLanguage());
				if (language != null) {
					map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
					map.put(BaseTemplate.PROPERTY_CAPTION, translatableText.getText());
					map.put(BaseTemplate.PROPERTY_DESCRIPTION, language.getIsoCode() + " (" + language.getLanguageLocalized(applicationInstanceData) + ")");
					return map;
				} else {
					map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.SYMBOL_QUESTIONMARK);
					map.put(BaseTemplate.PROPERTY_CAPTION, translatableText.getText());
					map.put(BaseTemplate.PROPERTY_DESCRIPTION, translatableText.getOriginalLanguage());
				}
			}
			return map;
		};
	}
}
