package org.teamapps.application.server.messaging.newsboard.views;

import org.teamapps.application.api.application.AbstractApplicationView;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.server.messaging.newsboard.NewsBoardUtils;
import org.teamapps.application.tools.EntityListModelBuilder;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.event.Event;
import org.teamapps.formatter.FileSizeFormatter;
import org.teamapps.model.controlcenter.NewsBoardMessageImage;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.session.SessionContext;

import java.util.*;

public class ImageListView extends AbstractApplicationView {

	private Table<NewsBoardMessageImage> table;
	private final EntityListModelBuilder<NewsBoardMessageImage> modelBuilder;

	public ImageListView(ApplicationInstanceData applicationInstanceData) {
		super(applicationInstanceData);
		modelBuilder = new EntityListModelBuilder<>(applicationInstanceData);
		//modelBuilder.setEntityStringFunction(s -> (String) propertyProvider.getValues(s, null).get(BaseTemplate.PROPERTY_CAPTION));
		SessionContext context = SessionContext.current();
		PropertyProvider<NewsBoardMessageImage> propertyProvider = (newsBoardMessageImage, collection) -> {
			Map<String, Object> map =new HashMap<>();
			map.put(BaseTemplate.PROPERTY_IMAGE, context.createFileLink(newsBoardMessageImage.getThumbnail().retrieveFile())); //TODO!
			map.put(BaseTemplate.PROPERTY_CAPTION, newsBoardMessageImage.getFileName());
			map.put(BaseTemplate.PROPERTY_DESCRIPTION, FileSizeFormatter.humanReadableByteCount(newsBoardMessageImage.getFile().getSize(), false, 2));
			return map;
		};
		table = modelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES, propertyProvider, 52);

	}

	public Component getComponent() {
		return table;
	}

	public NewsBoardMessageImage getSelectedImage() {
		return modelBuilder.getSelectedRecord();
	}

	public void removeSelectedImage() {
		modelBuilder.removeRecord(modelBuilder.getSelectedRecord());
	}

	public void setImages(List<NewsBoardMessageImage> images) {
		modelBuilder.setRecords(images);
	}

	public void addImages(List<NewsBoardMessageImage> images) {
		modelBuilder.addRecords(images);
	}


}
