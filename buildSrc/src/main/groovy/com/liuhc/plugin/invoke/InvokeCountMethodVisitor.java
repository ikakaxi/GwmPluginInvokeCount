package com.liuhc.plugin.invoke;

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

	/**
	 * Constructs a new {@link AdviceAdapter}.
	 */
	protected InvokeCountMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
		super(Opcodes.ASM6, methodVisitor, access, name, descriptor);
	}

	@Override
	protected void onMethodEnter() {
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(16, l0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
		mv.visitMethodInsn(INVOKESTATIC, "test/InvokeCountUtil", "getCurrentMethodName", "([Ljava/lang/StackTraceElement;)Ljava/lang/String;", false);
		mv.visitVarInsn(ASTORE, 0);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(18, l1);
		mv.visitFieldInsn(GETSTATIC, "test/InvokeCountUtil", "map", "Ljava/util/Map;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", true);
		Label l2 = new Label();
		mv.visitJumpInsn(IFNE, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(19, l3);
		mv.visitTypeInsn(NEW, "test/InvokeCountUtil$InvokeCountData");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "test/InvokeCountUtil$InvokeCountData", "<init>", "()V", false);
		mv.visitVarInsn(ASTORE, 1);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitLineNumber(20, l4);
		mv.visitFieldInsn(GETSTATIC, "test/InvokeCountUtil", "map", "Ljava/util/Map;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
		mv.visitInsn(POP);
		Label l5 = new Label();
		mv.visitJumpInsn(GOTO, l5);
		mv.visitLabel(l2);
		mv.visitLineNumber(22, l2);
		mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
		mv.visitFieldInsn(GETSTATIC, "test/InvokeCountUtil", "map", "Ljava/util/Map;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
		mv.visitTypeInsn(CHECKCAST, "test/InvokeCountUtil$InvokeCountData");
		mv.visitVarInsn(ASTORE, 1);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(23, l6);
		mv.visitVarInsn(ALOAD, 1);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitLineNumber(24, l7);
		mv.visitMethodInsn(INVOKEVIRTUAL, "test/InvokeCountUtil$InvokeCountData", "addCount", "()Ltest/InvokeCountUtil$InvokeCountData;", false);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLineNumber(25, l8);
		mv.visitMethodInsn(INVOKEVIRTUAL, "test/InvokeCountUtil$InvokeCountData", "updateTime", "()Ltest/InvokeCountUtil$InvokeCountData;", false);
		mv.visitInsn(POP);
		mv.visitLabel(l5);
		mv.visitLineNumber(27, l5);
		mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"test/InvokeCountUtil$InvokeCountData"}, 0, null);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "test/InvokeCountUtil$InvokeCountData", "toString", "()Ljava/lang/String;", false);
		mv.visitMethodInsn(INVOKESTATIC, "test/InvokeCountUtil", "log", "(Ljava/lang/String;)V", false);
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitLineNumber(28, l9);
		mv.visitInsn(RETURN);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitLocalVariable("invokeCountData", "Ltest/InvokeCountUtil$InvokeCountData;", null, l4, l2, 1);
		mv.visitLocalVariable("currentMethodName", "Ljava/lang/String;", null, l1, l10, 0);
		mv.visitLocalVariable("invokeCountData", "Ltest/InvokeCountUtil$InvokeCountData;", null, l6, l10, 1);
		mv.visitMaxs(3, 2);
		mv.visitEnd();
	}

	@Override
	protected void onMethodExit(int opcode) {
		super.onMethodExit(opcode);
	}
}
