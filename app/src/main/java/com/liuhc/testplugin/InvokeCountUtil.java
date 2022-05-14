package com.liuhc.testplugin;

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
		log(invokeCountData.toString());
	}

	private static void log(String message){
//		Log.e("test.InvokeCountUtil", message);
	}

	private static String getCurrentMethodName(StackTraceElement[] elements) {
		return elements[3].getClassName() + "." + elements[3].getMethodName();
	}
	public static class InvokeCountData {
		private int count;
		private long time;

		InvokeCountData() {
			this.count = 1;
			this.time = 0;
		}

		public InvokeCountData addCount() {
			count++;
			return this;
		}

		public InvokeCountData updateTime() {
			this.time = System.nanoTime() - this.time;
			return this;
		}

		@Override
		public String toString() {
			return "test.InvokeCountData{" +
					"count=" + count +
					", time=" + time +
					'}';
		}
	}
}
