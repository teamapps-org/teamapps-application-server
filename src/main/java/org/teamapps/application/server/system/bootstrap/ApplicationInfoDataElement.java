package org.teamapps.application.server.system.bootstrap;

import java.util.ArrayList;
import java.util.List;

public class ApplicationInfoDataElement {

	private String data;
	private List<String> dataAdded = new ArrayList<>();
	private List<String> dataRemoved = new ArrayList<>();

	public void added(String element) {
		dataAdded.add(element);
	}

	public void removed(String element) {
		dataRemoved.add(element);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public List<String> getDataAdded() {
		return dataAdded;
	}

	public void setDataAdded(List<String> dataAdded) {
		this.dataAdded = dataAdded;
	}

	public List<String> getDataRemoved() {
		return dataRemoved;
	}

	public void setDataRemoved(List<String> dataRemoved) {
		this.dataRemoved = dataRemoved;
	}

	@Override
	public String toString() {
		return "ApplicationInfoDataElement{" +
				"data='" + data + '\'' +
				", dataAdded=" + dataAdded +
				", dataRemoved=" + dataRemoved +
				'}';
	}
}
