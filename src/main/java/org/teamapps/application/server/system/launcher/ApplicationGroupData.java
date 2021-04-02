package org.teamapps.application.server.system.launcher;

import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.ManagedApplicationGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationGroupData {

	private final Icon icon;
	private final String title;
	private final int groupPosition;
	private final List<ApplicationData> applications = new ArrayList<>();

	public ApplicationGroupData(ManagedApplicationGroup applicationGroup, UserSessionData userSessionData) {
		this.icon = applicationGroup.getIcon() != null ? IconUtils.decodeIcon(applicationGroup.getIcon()) : null;
		this.title = userSessionData.getDictionary().getLocalized(applicationGroup.getTitleKey());
		this.groupPosition = applicationGroup.getListingPosition();
	}

	public ApplicationGroupData(Icon icon, String title, int groupPosition) {
		this.icon = icon;
		this.title = title;
		this.groupPosition = groupPosition;
	}

	public void addApplicationData(ApplicationData applicationData) {
		applications.add(applicationData);
	}

	public Icon getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public Integer getGroupPosition() {
		return groupPosition;
	}

	public List<ApplicationData> getSortedApplications() {
		return applications.stream()
				.sorted(Comparator.comparing(ApplicationData::getApplicationPosition))
				.collect(Collectors.toList());
	}

	public static List<ApplicationGroupData> getSortedGroups(Collection<ApplicationGroupData> groups) {
		return groups.stream()
				.sorted(Comparator.comparing(ApplicationGroupData::getGroupPosition))
				.collect(Collectors.toList());
	}
}
