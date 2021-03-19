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

	private final ApplicationInstanceData applicationInstanceData;
	private final List<AbstractPerspectiveBuilder> perspectiveBuilders;
	private Tree<AbstractPerspectiveBuilder> tree;
	private Map<AbstractPerspectiveBuilder, ApplicationPerspective> perspectiveByBuilderMap;

	public static PerspectiveMenuPanel createMenuPanel(ApplicationInstanceData applicationInstanceData, AbstractPerspectiveBuilder... perspectiveBuilders) {
		return createMenuPanel(applicationInstanceData, Arrays.asList(perspectiveBuilders));
	}

	public static PerspectiveMenuPanel createMenuPanel(ApplicationInstanceData applicationInstanceData, List<AbstractPerspectiveBuilder> perspectiveBuilders) {
		return new PerspectiveMenuPanel(applicationInstanceData, perspectiveBuilders);
	}

	public PerspectiveMenuPanel(ApplicationInstanceData applicationInstanceData, List<AbstractPerspectiveBuilder> perspectiveBuilders) {
		this.applicationInstanceData = applicationInstanceData;
		this.perspectiveBuilders = perspectiveBuilders;
		init();
	}

	private void init() {
		perspectiveByBuilderMap = new HashMap<>();
		List<AbstractPerspectiveBuilder> allowedPerspectiveBuilders = perspectiveBuilders.stream().filter(p -> p.isPerspectiveAccessible(applicationInstanceData)).collect(Collectors.toList());
		ListTreeModel<AbstractPerspectiveBuilder> treeModel = new ListTreeModel<>(allowedPerspectiveBuilders);
		tree = new Tree<>(treeModel);
		tree.setShowExpanders(false);
		tree.setEntryTemplate(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES);
		tree.setPropertyExtractor((builder, propertyName) -> switch (propertyName) {
			case BaseTemplate.PROPERTY_ICON -> builder.getIcon();
			case BaseTemplate.PROPERTY_CAPTION -> applicationInstanceData.getLocalized(builder.getTitleKey());
			case BaseTemplate.PROPERTY_DESCRIPTION -> applicationInstanceData.getLocalized(builder.getDescriptionKey());
			default -> null;
		});
		tree.onNodeSelected.addListener(builder -> openPerspective(builder));
		if (!treeModel.getRecords().isEmpty()) {
			tree.setSelectedNode(treeModel.getRecords().get(0));
		}
	}

	public void openPerspective(AbstractPerspectiveBuilder builder) {
		ApplicationPerspective applicationPerspective = perspectiveByBuilderMap.get(builder);
		if (applicationPerspective == null) {
			applicationPerspective = builder.build(applicationInstanceData, null);
			perspectiveByBuilderMap.put(builder, applicationPerspective);
		}
		tree.setSelectedNode(builder);
		applicationInstanceData.showPerspective(applicationPerspective.getPerspective());
	}

	public void addInstantiatedPerspective(AbstractPerspectiveBuilder builder, ApplicationPerspective perspective) {
		perspectiveByBuilderMap.put(builder, perspective);
	}

	public Component getComponent() {
		return tree;
	}
}
