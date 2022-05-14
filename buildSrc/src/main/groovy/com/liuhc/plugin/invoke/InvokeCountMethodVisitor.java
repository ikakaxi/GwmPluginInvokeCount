package com.liuhc.plugin.invoke;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 1:27 下午
 */
class InvokeCountMethodVisitor extends MethodVisitor {

	public InvokeCountMethodVisitor(MethodVisitor methodVisitor) {
		super(Opcodes.ASM5, methodVisitor);
	}
}
