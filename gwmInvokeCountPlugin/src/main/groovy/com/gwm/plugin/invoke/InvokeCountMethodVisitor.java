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

	private final InvokeCountConfig config;
	private final String className;

	/**
	 * Constructs a new {@link AdviceAdapter}.
	 */
	protected InvokeCountMethodVisitor(MethodVisitor methodVisitor, InvokeCountConfig config, int access, String className, String name, String descriptor) {
		super(Opcodes.ASM6, methodVisitor, access, name, descriptor);
		this.config = config;
		this.className = className;
	}

	@Override
	protected void onMethodEnter() {
		Label classNameLabel = new Label();
		mv.visitLabel(classNameLabel);
		mv.visitLdcInsn(className);
		// 因为visitMethodInsn方法传入的格式是com/a/b/c/Demo这种格式的，所以要把com.a.b.c.Demo转换一下格式
		String transformInvokeClass = config.invokeClass.replace(".", "/");
		mv.visitMethodInsn(INVOKESTATIC, transformInvokeClass, config.invokeMethod, "(Ljava/lang/String;)V", false);
		System.err.println("--------------" + transformInvokeClass + "#" + config.invokeMethod + "插桩成功--------------");
	}

	@Override
	protected void onMethodExit(int opcode) {
		super.onMethodExit(opcode);
	}
}
