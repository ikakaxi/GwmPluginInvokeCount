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
		String invokeMethodName = getInvokeMethodName(Thread.currentThread().getStackTrace());
		InvokeCountData invokeCountData;
		if (!map.containsKey(invokeMethodName)) {
			invokeCountData = new InvokeCountData(invokeMethodName);
			map.put(invokeMethodName, invokeCountData);
		} else {
			invokeCountData = map.get(invokeMethodName);
			invokeCountData
					.addCount()
					.updateDuration();
		}
		log(invokeCountData.toString());
	}

	private static void log(String message) {
		Log.e("InvokeCountUtil", message);
	}

	private static final int index = 4;
	private static String getInvokeMethodName(StackTraceElement[] elements) {
		return elements[index].getClassName() + "." + elements[index].getMethodName();
	}

	public static class InvokeCountData {
		//System.nanoTime()获取的是纳秒，1000纳秒是1微秒，1000微秒是1毫秒，1000毫秒是1秒
		private static final long SECODE = 1000 * 1000 * 1000;
		private int count;
		private long time;
		private long duration;
		private String invokeMethodName;

		InvokeCountData(String invokeMethodName) {
			this.count = 1;
			this.time = System.nanoTime();
			this.invokeMethodName = invokeMethodName;
		}

		public InvokeCountData addCount() {
			count++;
			return this;
		}

		public InvokeCountData updateDuration() {
			this.duration = System.nanoTime() - this.time;
			return this;
		}

		@Override
		public String toString() {
			return "InvokeCountData{" +
					"调用方=" + invokeMethodName +
					"调用次数=" + count +
					", 持续时间=" + (duration / SECODE) +
					"秒}";
		}
	}
}
