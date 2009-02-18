package com.koonen.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author dryganets
 * 
 */
public class Enumeration {

	private static Map<Class<?>, Map<String, Enumeration>> map = new HashMap<Class<?>, Map<String, Enumeration>>();

	protected static void add(Class<?> clazz, Enumeration enumeration) {
		Map<String, Enumeration> enumMap = map.get(clazz);
		if (enumMap == null) {
			enumMap = new HashMap<String, Enumeration>();
			map.put(clazz, enumMap);
		}
		enumMap.put(enumeration.getValue(), enumeration);
	}

	public static Enumeration valueOf(Class<?> clazz, String name) {
		Enumeration result = null;
		Map<String, Enumeration> enumMap = map.get(clazz);
		if (enumMap != null) {
			result = enumMap.get(name);
		}
		return result;
	}

	private String name;
	private String value;

	public Enumeration(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Collection<Enumeration> values(Class<?> clazz) {
		Collection<Enumeration> result = null;
		Map<String, Enumeration> enum_ = map.get(clazz);

		if (enum_ != null) {
			result = enum_.values();
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
