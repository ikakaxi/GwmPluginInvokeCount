package com.liuhc.testplugin;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 2:14 下午
 */
class InvokeCountUtil {

	public static Map<String, InvokeCountData> map = new HashMap<>();

	public static void count() {
		StackTraceElement targetElement = getInvokeStackTraceElement();
		String invokeMethodName = getInvokeMethodName(targetElement);
		InvokeCountData invokeCountData;
		if (!map.containsKey(invokeMethodName)) {
			invokeCountData = new InvokeCountData();
			map.put(invokeMethodName, invokeCountData);
		} else {
			invokeCountData = map.get(invokeMethodName);
			assert invokeCountData != null;
			invokeCountData.addCount();
			invokeCountData.updateDuration();
		}
		log(targetElement, invokeCountData.toString());
	}

	private static void log(StackTraceElement targetElement, String message) {
		Log.e("InvokeCountUtil", getClickableHeader(targetElement) + message);
	}

	private static String getClickableHeader(StackTraceElement targetElement) {
		return "(" + targetElement.getFileName() + ":" + targetElement.getLineNumber() + ")";
	}

	private static final int index = 5;

	private static StackTraceElement getInvokeStackTraceElement() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		return elements[index];
	}

	private static String getInvokeMethodName(StackTraceElement elements) {
		return elements.getClassName() + "." + elements.getMethodName();
	}

	public static class InvokeCountData {
		//System.nanoTime()获取的是纳秒，1000纳秒是1微秒，1000微秒是1毫秒，1000毫秒是1秒
		private static final long SECODE = 1000 * 1000 * 1000;
		private int count;
		private final long time;
		private long duration;

		InvokeCountData() {
			this.count = 1;
			this.time = System.nanoTime();
		}

		public void addCount() {
			count++;
		}

		public void updateDuration() {
			this.duration = System.nanoTime() - this.time;
		}

		@NonNull
		@Override
		public String toString() {
			return "调用次数=" + count + " ,持续时间=" + (duration / SECODE) + "秒";
		}
	}
}
