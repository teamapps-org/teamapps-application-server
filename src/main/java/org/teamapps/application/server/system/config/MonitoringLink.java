package org.teamapps.application.server.system.config;

import java.util.ArrayList;
import java.util.List;

public class MonitoringLink {

	private String title = "title";
	private String link = "https://...";
	private List<MonitoringLink> childLinks = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<MonitoringLink> getChildLinks() {
		return childLinks;
	}

	public void setChildLinks(List<MonitoringLink> childLinks) {
		this.childLinks = childLinks;
	}
}
