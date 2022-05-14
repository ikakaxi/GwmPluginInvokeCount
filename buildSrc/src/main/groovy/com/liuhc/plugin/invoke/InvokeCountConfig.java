package com.liuhc.plugin.invoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvokeCountConfig {

	public Set<String> invokeList;

	private final Map<String, List<String>> classToMethodsMap = new HashMap<>();

	void init() {
		if (invokeList == null) {
			return;
		}
		for (String s : invokeList) {
			String[] split = s.split("#");
			String key = split[0];
			String value = split[1];
			if (!classToMethodsMap.containsKey(key)) {
				classToMethodsMap.put(key, new ArrayList<>());
			}
			classToMethodsMap.get(key).add(value);
		}
	}

	boolean containMethod(String className, String method) {
		if (classToMethodsMap.containsKey(className)) {
			return classToMethodsMap.get(className).contains(method);
		}
		return false;
	}

}
