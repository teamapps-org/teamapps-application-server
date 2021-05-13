package org.teamapps.application.server.system.config;

import java.util.Arrays;
import java.util.List;

public class MonitoringDashboardConfig {

	private List<MonitoringLink> monitoringLinks = Arrays.asList(new MonitoringLink());

	public List<MonitoringLink> getMonitoringLinks() {
		return monitoringLinks;
	}

	public void setMonitoringLinks(List<MonitoringLink> monitoringLinks) {
		this.monitoringLinks = monitoringLinks;
	}
}
