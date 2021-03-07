package org.teamapps.application.server.system.application;

import org.teamapps.application.api.application.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.model.ListTreeModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerspectiveMenuPanel {

	public static Component createMenuPanel(ApplicationInstanceData applicationInstanceData, AbstractPerspectiveBuilder... perspectiveBuilders) {
		return createMenuPanel(applicationInstanceData, Arrays.asList(perspectiveBuilders));
	}

	public static Component createMenuPanel(ApplicationInstanceData applicationInstanceData, List<AbstractPerspectiveBuilder> perspectiveBuilders) {
		Map<AbstractPerspectiveBuilder, ApplicationPerspective> perspectiveByBuilderMap = new HashMap<>();
		List<AbstractPerspectiveBuilder> allowedPerspectiveBuilders = perspectiveBuilders.stream().filter(p -> p.isPerspectiveAccessible(applicationInstanceData)).collect(Collectors.toList());
		ListTreeModel<AbstractPerspectiveBuilder> treeModel = new ListTreeModel<>(allowedPerspectiveBuilders);
		Tree<AbstractPerspectiveBuilder> tree = new Tree<>(treeModel);
		tree.setShowExpanders(false);
		tree.setEntryTemplate(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES);
		tree.setPropertyExtractor((builder, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> builder.getIcon();
			case BaseTemplate.PROPERTY_CAPTION -> applicationInstanceData.getLocalized(builder.getTitleKey());
			case BaseTemplate.PROPERTY_DESCRIPTION -> applicationInstanceData.getLocalized(builder.getDescriptionKey());
			default -> null;
		});
		tree.onNodeSelected.addListener(builder -> {
			ApplicationPerspective applicationPerspective = perspectiveByBuilderMap.get(builder);
			if (applicationPerspective == null) {
				applicationPerspective = builder.build(applicationInstanceData, null);
				perspectiveByBuilderMap.put(builder, applicationPerspective);
			}
			applicationInstanceData.showPerspective(applicationPerspective.getPerspective());
		});
		return tree;
	}

}
