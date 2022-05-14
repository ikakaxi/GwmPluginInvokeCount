package com.liuhc.plugin.invoke;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 1:27 下午
 */
class InvokeCountMethodVisitor extends MethodVisitor {

	/**
	 * Constructs a new {@link AdviceAdapter}.
	 */
	public InvokeCountMethodVisitor(MethodVisitor methodVisitor) {
		super(Opcodes.ASM6, methodVisitor);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		//方法执行前插入
	}

	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		//方法执行后插入
	}

}
