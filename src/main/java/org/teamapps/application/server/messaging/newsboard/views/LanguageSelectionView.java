package org.teamapps.application.server.messaging.newsboard.views;

import org.teamapps.application.api.application.AbstractApplicationView;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.messaging.newsboard.NewsBoardUtils;
import org.teamapps.application.tools.EntityListModelBuilder;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.event.Event;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.Collections;
import java.util.List;

public class LanguageSelectionView extends AbstractApplicationView {

	public Event<String> onLanguageSelection = new Event<>();
	private Table<String> table;
	private final EntityListModelBuilder<String> modelBuilder;

	public LanguageSelectionView(ApplicationInstanceData applicationInstanceData) {
		super(applicationInstanceData);
		PropertyProvider<String> propertyProvider = NewsBoardUtils.createLanguageSelectionPropertyProvider(applicationInstanceData);
		modelBuilder = new EntityListModelBuilder<>(applicationInstanceData);
		modelBuilder.setEntityStringFunction(s -> (String) propertyProvider.getValues(s, null).get(BaseTemplate.PROPERTY_CAPTION));
		table = modelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, propertyProvider, 30);
		modelBuilder.onSelectedRecordChanged.addListener(language -> onLanguageSelection.fire(language));
	}

	public Component getComponent() {
		return table;
	}

	public void setOwnLanguage() {
		modelBuilder.setRecords(Collections.singletonList(NewsBoardUtils.USER_LANGUAGES));
	}

	public void setLanguages(List<String> languages) {
		modelBuilder.setRecords(languages);
	}
}
