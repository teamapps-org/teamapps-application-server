package org.teamapps.application.server.system.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValueCompare {

	private boolean different;
	private Map<String, Object> valueByKey = new HashMap<>();

	public static ValueCompare create(Object a, Object b) {
		return new ValueCompare().check(a, b);
	}

	public static ValueCompare create(String key, Object a, Object b) {
		return new ValueCompare().check(key, a, b);
	}

	public ValueCompare check(Object a, Object b) {
		if (!Objects.equals(a, b)) {
			different = true;
		}
		return this;
	}

	public ValueCompare check(String key, Object value, Object existingValue) {
		valueByKey.put(key, value);
		return check(value, existingValue);
	}

	public boolean isDifferent() {
		return different;
	}

	public String getString(String key) {
		return (String) valueByKey.get(key);
	}

	public Integer getInt(String key) {
		return (Integer) valueByKey.get(key);
	}

	public Object get(String key) {
		return valueByKey.get(key);
	}
}
