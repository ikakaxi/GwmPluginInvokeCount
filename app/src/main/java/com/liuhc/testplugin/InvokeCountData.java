package com.liuhc.testplugin;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 2:33 下午
 */
class InvokeCountData {
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
		return "InvokeCountData{" +
				"count=" + count +
				", time=" + time +
				'}';
	}
}
