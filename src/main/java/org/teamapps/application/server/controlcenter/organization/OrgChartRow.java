package org.teamapps.application.server.controlcenter.organization;

import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.RoleType;
import org.teamapps.model.controlcenter.UserRoleAssignment;

import java.util.ArrayList;
import java.util.List;

public class OrgChartRow {

	private final OrgChartNode orgUnitNode;
	private final OrgChartNode leaderNode;
	private final OrgChartNode assistantNode;
	private final OrgChartNode otherNode;

	public static List<OrgChartRow> createRows(OrgChartNode orgChartNode) {
		List<OrgChartRow> rows = new ArrayList<>();
		List<OrgChartNode> leaderNodes = orgChartNode.getNodesByType(RoleType.LEADER);
		List<OrgChartNode> assistantNodes = orgChartNode.getNodesByType(RoleType.MENTOR, RoleType.ASSISTANT, RoleType.ADMINISTRATOR);
		List<OrgChartNode> otherNodes = orgChartNode.getNodesByType(RoleType.OTHER);
		int rowsCount = Math.max(1, Math.max(leaderNodes.size(), Math.max(assistantNodes.size(),  otherNodes.size())));
		for (int i = 0; i < rowsCount; i++) {
			OrgChartNode orgUnitNode = i == 0 ? orgChartNode : null;
			OrgChartNode leaderNode = leaderNodes.size() > i ? leaderNodes.get(i) : null;
			OrgChartNode assistantNode = assistantNodes.size() > i ? assistantNodes.get(i) : null;
			OrgChartNode otherNode = otherNodes.size() > i ? otherNodes.get(i) : null;
			rows.add(new OrgChartRow(orgUnitNode, leaderNode, assistantNode, otherNode));
		}
		return rows;
	}

	public OrgChartRow(OrgChartNode orgUnitNode, OrgChartNode leaderNode, OrgChartNode assistantNode, OrgChartNode otherNode) {
		this.orgUnitNode = orgUnitNode;
		this.leaderNode = leaderNode;
		this.assistantNode = assistantNode;
		this.otherNode = otherNode;
	}

	public boolean match(String query) {
		if (orgUnitNode != null && orgUnitNode.matches(query)) return true;
		if (leaderNode != null && leaderNode.matches(query)) return true;
		if (assistantNode != null && assistantNode.matches(query)) return true;
		if (otherNode != null && otherNode.matches(query)) return true;
		return false;
	}

	public OrgChartNode getOrgUnitNode() {
		return orgUnitNode;
	}

	public OrgChartNode getLeaderNode() {
		return leaderNode;
	}

	public OrgChartNode getAssistantNode() {
		return assistantNode;
	}

	public OrgChartNode getOtherNode() {
		return otherNode;
	}
}
