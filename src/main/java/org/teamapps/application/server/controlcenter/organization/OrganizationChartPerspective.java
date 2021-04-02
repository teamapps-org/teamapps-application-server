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
package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.controlcenter.users.UserUtils;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.Templates;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.localize.TranslatableTextUtils;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.model.controlcenter.OrganizationField;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.UserRoleAssignment;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.charting.common.GraphNodeIcon;
import org.teamapps.ux.component.charting.common.GraphNodeImage;
import org.teamapps.ux.component.charting.tree.BaseTreeGraphNode;
import org.teamapps.ux.component.charting.tree.TreeGraph;
import org.teamapps.ux.component.charting.tree.TreeGraphNode;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrganizationChartPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private TreeGraph<OrgNode> treeGraph;
	private float zoomFactor = 1;
	private List<TreeGraphNode<OrgNode>> treeNodes;

	public OrganizationChartPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.PIECES, getLocalized("organizationField.organizationFields"), null));
		masterView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.75f));

		ToolbarButtonGroup buttonGroup = masterView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton standardViewButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ELEMENTS_HIERARCHY, getLocalized("organizationChart.standardView"), getLocalized("organizationChart.standardView.desc")));
		ToolbarButton compactViewButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ELEMENTS_TREE, getLocalized("organizationChart.compactView"), getLocalized("organizationChart.compactView.desc")));

		buttonGroup = masterView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton zoomInButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ZOOM_IN, getLocalized(Dictionary.ZOOM_IN), getLocalized(Dictionary.ZOOM_IN)));
		ToolbarButton zoomOutButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ZOOM_OUT, getLocalized(Dictionary.ZOOM_OUT), getLocalized(Dictionary.ZOOM_OUT)));

		standardViewButton.onClick.addListener(() -> {
			treeGraph.setCompact(false);
			//treeGraph.setNodes(treeNodes);
		});
		compactViewButton.onClick.addListener(() -> {
			treeGraph.setCompact(true);
			//treeGraph.setNodes(Collections.emptyList());
		});
		zoomInButton.onClick.addListener(() -> {
			zoomFactor = zoomFactor * 1.5f;
			treeGraph.setZoomFactor(zoomFactor);
			treeGraph.setNodes(treeNodes);
		});
		zoomOutButton.onClick.addListener(() -> {
			zoomFactor = zoomFactor / 1.5f;
			treeGraph.setZoomFactor(zoomFactor);
			treeGraph.setNodes(treeNodes);
		});

		if (OrganizationField.getCount() == 0) {
			return;
		}
		OrganizationField organizationField = getOrganizationField() != null ? getOrganizationField() : OrganizationField.getAll().get(0);
		Set<OrganizationUnit> units = new HashSet<>(OrganizationUnit.getAll()); //todo acl
		Set<OrganizationUnit> allowedUnits = units.stream().flatMap(unit -> getRootChain(unit).stream()).collect(Collectors.toSet());
		createUi(allowedUnits, organizationField, masterView);
	}

	private void createUi(Set<OrganizationUnit> allowedUnits, OrganizationField organizationField, View masterView) {
		treeGraph = new TreeGraph<>();

		Map<OrganizationUnit, List<UserRoleAssignment>> roleAssignmentsByOrganizationUnit = UserRoleAssignment.getAll().stream()
				.filter(userRoleAssignment -> allowedUnits.contains(userRoleAssignment.getOrganizationUnit()))
				.filter(userRoleAssignment -> organizationField.equals(userRoleAssignment.getRole().getOrganizationField()))
				.collect(Collectors.groupingBy(UserRoleAssignment::getOrganizationUnit));


		treeNodes = new ArrayList<>();
		Map<OrganizationUnit, TreeGraphNode<OrgNode>> nodeByOrgUnit = new HashMap<>();

		for (OrganizationUnit organizationUnit : allowedUnits) {
			List<UserRoleAssignment> userRoleAssignments = roleAssignmentsByOrganizationUnit.get(organizationUnit);
			TreeGraphNode<OrgNode> node = createNode(organizationUnit, userRoleAssignments != null ? userRoleAssignments : Collections.emptyList());
			treeNodes.add(node);
			nodeByOrgUnit.put(organizationUnit, node);
		}
		treeNodes.forEach(node -> {
			node.setParent(nodeByOrgUnit.get(node.getRecord().getOrganizationUnit().getParent()));
			node.setParentExpandable(node.getParent() != null);
		});

		treeGraph.setPropertyProvider(createPropertyProvider());
		treeGraph.setHorizontalSiblingGap(40);
		treeGraph.setHorizontalNonSignlingGap(60);
		treeGraph.setVerticalLayerGap(75);
		treeGraph.setNodes(treeNodes);

		masterView.setComponent(treeGraph);
	}

	private TreeGraphNode<OrgNode> createNode(OrganizationUnit unit, List<UserRoleAssignment> userRoleAssignments) {
		TreeGraphNode<OrgNode> node = new TreeGraphNode<>();
		UserRoleAssignment mainAssignment = userRoleAssignments.stream()
				//.filter(assignment -> assignment.getRole().getParent() == null)
				.sorted(getRoleAssignmentComparator())
				.findAny()
				.orElse(null);
		node.setRecord(new OrgNode(unit, mainAssignment));

		node.setTemplate(Templates.ORGANIZATION_GRAPH_TEMPLATE);
		node.setWidth(240);
		node.setHeight(68);
		node.setBorderRadius(10);
		node.setBorderWidth(1f);
		node.setConnectorLineColor(Color.MATERIAL_BLUE_700);
		node.setBorderColor(Color.MATERIAL_BLUE_700);
		node.setBackgroundColor(Color.MATERIAL_BLUE_100);
		node.setExpanded(false);

		if (mainAssignment != null && UserUtils.getProfilePictureLink(mainAssignment.getUser(), userSessionData) != null) {
			String pictureLink = UserUtils.getProfilePictureLink(mainAssignment.getUser(), userSessionData);
			node.setImage(new GraphNodeImage(pictureLink, 70, 70).setBorderColor(Color.MATERIAL_BLUE_700).setBorderWidth(1f).setCenterLeftDistance(0).setCenterTopDistance(34).setCornerShape(GraphNodeImage.CornerShape.CIRCLE));
		} else {
			node.setIcon(new GraphNodeIcon(IconUtils.decodeIcon(unit.getIcon() != null ? unit.getIcon() : unit.getType().getIcon()), 54));
		}


		List<BaseTreeGraphNode<OrgNode>> sideNodes = userRoleAssignments.stream().filter(assignment -> assignment != mainAssignment).map(assignment -> createSideNode(unit, assignment)).collect(Collectors.toList());
		node.setSideListNodes(sideNodes);

		return node;
	}

	private Comparator<UserRoleAssignment> getRoleAssignmentComparator() {
		return  (o1, o2) -> getSortScore(o1) > getSortScore(o2) ? -1 : 0;
	}

	private int getSortScore(UserRoleAssignment assignment) {
		int score = assignment.getRole().getParent() != null ? 0 : 10;
		score += assignment.getUser().getProfilePicture() != null ? 1 : 0;
		return score;
	}

	private BaseTreeGraphNode<OrgNode> createSideNode(OrganizationUnit unit, UserRoleAssignment userRoleAssignment) {
		BaseTreeGraphNode<OrgNode> node = new BaseTreeGraphNode<>();
		node.setTemplate(Templates.ORGANIZATION_GRAPH_SMALL_TEMPLATE);
		node.setRecord(new OrgNode(unit, userRoleAssignment));
		node.setWidth(200);
		node.setHeight(40);
		node.setBorderRadius(7);
		node.setBorderWidth(1f);
		node.setBorderColor(Color.MATERIAL_GREEN_900);
		node.setBackgroundColor(Color.MATERIAL_GREEN_100);
		node.setConnectorLineColor(Color.MATERIAL_BLUE_700);
		String pictureLink = UserUtils.getProfilePictureLink(userRoleAssignment.getUser(), userSessionData);
		if (pictureLink != null) {
			node.setImage(new GraphNodeImage(pictureLink, 55, 55).setBorderColor(Color.MATERIAL_BLUE_700).setBorderWidth(1f).setCenterLeftDistance(0).setCenterTopDistance(20).setCornerShape(GraphNodeImage.CornerShape.CIRCLE));
		} else {
			node.setIcon(new GraphNodeIcon(IconUtils.decodeIcon(unit.getIcon() != null ? unit.getIcon() : unit.getType().getIcon()), 32));
		}

		return node;
	}

	public static class OrgNode {
		private final OrganizationUnit organizationUnit;
		private final UserRoleAssignment userRoleAssignment;

		public OrgNode(OrganizationUnit organizationUnit, UserRoleAssignment userRoleAssignment) {
			this.organizationUnit = organizationUnit;
			this.userRoleAssignment = userRoleAssignment;
		}

		public OrganizationUnit getOrganizationUnit() {
			return organizationUnit;
		}

		public UserRoleAssignment getUserRoleAssignment() {
			return userRoleAssignment;
		}
	}

	private List<OrganizationUnit> getRootChain(OrganizationUnit unit) {
		List<OrganizationUnit> chain = new ArrayList<>();
		while (unit != null) {
			chain.add(unit);
			unit = unit.getParent();
		}
		return chain;
	}


	private PropertyProvider<OrgNode> createPropertyProvider() {
		Function<TranslatableText, String> translatableTextExtractor = TranslatableTextUtils.createTranslatableTextExtractor(userSessionData.getRankedLanguages());
		return (orgNode, collection) -> {
			OrganizationUnit unit = orgNode.getOrganizationUnit();
			String prefix = "";
			String abbreviation = translatableTextExtractor.apply(unit.getType().getAbbreviation());
			if (abbreviation != null) {
				prefix = abbreviation + "-";
			}
			String unitName = prefix + translatableTextExtractor.apply(unit.getName());
			String name = translatableTextExtractor.apply(unit.getType().getName());
			String role = null;
			if (orgNode.getUserRoleAssignment() != null) {
				UserRoleAssignment assignment = orgNode.getUserRoleAssignment();
				name = assignment.getUser().getFirstName() + " " + assignment.getUser().getLastName();
				role = translatableTextExtractor.apply(assignment.getRole().getTitle());
			}

			Map<String, Object> map = new HashMap<>();
			map.put(Templates.PROPERTY_CAPTION, unitName);
			map.put(Templates.PROPERTY_DESCRIPTION, name);
			map.put(Templates.PROPERTY_LINE3, role);
			return map;
		};
	}

}

