package com.liuhc.plugin.invoke;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author liuhc
 * date 2022-5-14
 * 统计某方法调用次数
 */
public class InvokeCountClassVisitor extends ClassVisitor {

	private final InvokeCountConfig config;
	private final String className;

	public InvokeCountClassVisitor(int i, ClassVisitor classVisitor, InvokeCountConfig config, String className) {
		super(i, classVisitor);
		this.config = config;
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
		config.init();
		if (config.containMethod(className, name)) {
			return new InvokeCountMethodVisitor(methodVisitor, access, name, desc);
		}
		return methodVisitor;
	}
}
