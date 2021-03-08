package org.teamapps.application.server.system.utils;

import org.teamapps.application.api.privilege.Privilege;
import org.teamapps.application.api.privilege.PrivilegeObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValueConverterUtils {


	public static String compressPrivilegeNames(List<Privilege> privileges) {
		if (privileges == null || privileges.isEmpty()) {
			return null;
		} else {
			return privileges.stream().map(Privilege::getName).collect(Collectors.joining("\n"));
		}

	}

	public static String compressPrivilegeObjectIds(List<PrivilegeObject> privilegeObjects) {
		if (privilegeObjects == null || privilegeObjects.isEmpty()) {
			return null;
		} else {
			return privilegeObjects.stream()
					.map(PrivilegeObject::getId)
					.map(id -> "" + id)
					.collect(Collectors.joining("\n"));
		}
	}

	public static String compressStringList(List<String> list) {
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return String.join("\n", list);
		}
	}

	public static List<String> decompressToStringList(String value) {
		if (value == null || value.isBlank()) {
			return null;
		} else {
			return Arrays.stream(value.split("\n"))
					.filter(n -> n != null && !n.isBlank())
					.collect(Collectors.toList());
		}
	}

	public static List<Integer> decompressIds(String ids) {
		if (ids == null || ids.isBlank()) {
			return null;
		} else {
			return Arrays.stream(ids.split("\n"))
					.filter(n -> n != null && !n.isBlank())
					.map(Integer::parseInt)
					.collect(Collectors.toList());
		}
	}
}
