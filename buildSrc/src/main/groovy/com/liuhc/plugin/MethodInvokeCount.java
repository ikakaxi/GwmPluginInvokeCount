package com.liuhc.plugin;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author liuhc
 * @date 2022-5-14
 * 统计某方法调用次数
 */
public class MethodInvokeCount extends ClassVisitor {
	public MethodInvokeCount(int i, ClassVisitor classVisitor,String className) {
		super(i, classVisitor);
	}

	@Override
	public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
		MethodVisitor methodVisitor = cv.visitMethod(i, s, s1, s2, strings);
		methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, i, s, s1) {
			boolean inject = true;

			@Override
			public AnnotationVisitor visitAnnotation(String s, boolean b) {
				return super.visitAnnotation(s, b);
			}

			@Override
			protected void onMethodEnter() {
				//方法进入时期
				if (inject) {
					//这里就是之前使用ASM插件生成的统计时间代码
					mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
					mv.visitLdcInsn("this is asm input");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

					mv.visitTypeInsn(NEW, "java/lang/Throwable");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Throwable", "<init>", "()V", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 1);

					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
					mv.visitMethodInsn(INVOKESTATIC, "com/liuhc/testplugin/TimeManager", "addStartTime", "(Ljava/lang/String;J)V", false);
				}
			}

			@Override
			protected void onMethodExit(int i) {
				//方法结束时期
				if (inject) {
					//计算方法耗时
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
					mv.visitMethodInsn(INVOKESTATIC, "com/liuhc/testplugin/TimeManager", "addEndTime", "(Ljava/lang/String;J)V", false);

					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "com/liuhc/testplugin/TimeManager", "calcuteTime", "(Ljava/lang/String;)V", false);
				}
			}
		};
		return methodVisitor;
	}
}
