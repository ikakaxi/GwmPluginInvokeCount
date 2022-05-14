package com.liuhc.testplugin;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 2:14 下午
 */
class InvokeCountUtil {

	public static Map<String, InvokeCountData> map = new HashMap<>();

	public static void count() {
		String currentMethodName = getCurrentMethodName(Thread.currentThread().getStackTrace());
		InvokeCountData invokeCountData;
		if (!map.containsKey(currentMethodName)) {
			invokeCountData = new InvokeCountData();
			map.put(currentMethodName, invokeCountData);
		} else {
			invokeCountData = map.get(currentMethodName);
			invokeCountData
					.addCount()
					.updateTime();
		}
		LogUtil.log(invokeCountData.toString());
	}

	private static String getCurrentMethodName(StackTraceElement[] elements) {
		return elements[3].getClassName() + "." + elements[3].getMethodName();
	}
}
