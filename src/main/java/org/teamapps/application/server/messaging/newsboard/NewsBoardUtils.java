package org.teamapps.application.server.messaging.newsboard;

import edu.emory.mathcs.backport.java.util.Collections;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.model.controlcenter.NewsBoardMessage;
import org.teamapps.model.controlcenter.NewsBoardMessageTranslation;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewsBoardUtils {

	public static final String USER_LANGUAGES = "user-languages";

	public static List<String> getEffectiveLanguages(String selectedLanguage, ApplicationInstanceData applicationInstanceData) {
		if (USER_LANGUAGES.equals(selectedLanguage)) {
			return applicationInstanceData.getUser().getRankedLanguages();
		} else {
			return Collections.singletonList(selectedLanguage);
		}
	}

	public static PropertyProvider<String> createLanguageSelectionPropertyProvider(ApplicationInstanceData applicationInstanceData) {
		return (value, collection) -> {
			Map<String, Object> map = new HashMap<>();
			if (USER_LANGUAGES.equals(value)) {
				map.put(BaseTemplate.PROPERTY_ICON, ApplicationIcons.USER);
				map.put(BaseTemplate.PROPERTY_CAPTION, applicationInstanceData.getLocalized("newsBoard.ownLanguages"));
			} else {
				Language language = Language.getLanguageByIsoCode(value);
				if (language != null) {
					map.put(BaseTemplate.PROPERTY_ICON, language.getIcon());
					map.put(BaseTemplate.PROPERTY_CAPTION, language.getLanguageLocalized(applicationInstanceData));
				}
			}
			return map;
		};
	}

	public static ComboBox<String> createLanguageSelectionComboBox(List<String> languages, ApplicationInstanceData applicationInstanceData) {
		languages.add(0, USER_LANGUAGES);
		ComboBox<String> recordComboBox = ComboBoxUtils.createRecordComboBox(languages, NewsBoardUtils.createLanguageSelectionPropertyProvider(applicationInstanceData), BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		return recordComboBox;
	}


	public static List<Language> getUsedLanguages(List<NewsBoardMessage> messages) {
		Stream<Language> languageStream = messages.stream().map(message -> Language.getLanguageByIsoCode(message.getLanguage())).filter(Objects::nonNull);
		Stream<Language> translationLanguagesStream = messages.stream().flatMap(message -> message.getTranslations().stream()).map(translation -> Language.getLanguageByIsoCode(translation.getLanguage())).filter(Objects::nonNull);
		return Stream.concat(languageStream, translationLanguagesStream).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet()
				.stream()
				.sorted(Map.Entry.<Language, Long>comparingByValue().reversed())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public static List<String> getUsedLanguageValues(List<NewsBoardMessage> messages) {
		Stream<String> languageStream = messages.stream().map(NewsBoardMessage::getLanguage).filter(Objects::nonNull);
		Stream<String> translationLanguagesStream = messages.stream().flatMap(message -> message.getTranslations().stream()).map(NewsBoardMessageTranslation::getLanguage).filter(Objects::nonNull);
		return Stream.concat(languageStream, translationLanguagesStream).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet()
				.stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}
}
