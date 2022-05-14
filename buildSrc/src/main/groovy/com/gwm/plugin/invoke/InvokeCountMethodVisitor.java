package com.gwm.plugin.invoke;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/14 on 1:27 下午
 */
class InvokeCountMethodVisitor extends AdviceAdapter {

	private final String className;

	/**
	 * Constructs a new {@link AdviceAdapter}.
	 */
	protected InvokeCountMethodVisitor(MethodVisitor methodVisitor, int access, String className, String name, String descriptor) {
		super(Opcodes.ASM6, methodVisitor, access, name, descriptor);
		this.className = className;
	}

	@Override
	protected void onMethodEnter() {
		Label classNameLabel = new Label();
		mv.visitLabel(classNameLabel);
		mv.visitLdcInsn(className);
		mv.visitMethodInsn(INVOKESTATIC, "com/liuhc/testplugin/InvokeCountUtil", "count", "(Ljava/lang/String;)V", false);
	}

	@Override
	protected void onMethodExit(int opcode) {
		super.onMethodExit(opcode);
	}
}
