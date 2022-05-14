package com.liuhc.plugin

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/13 on 10:11 下午
 */
class MethodTotal(i: Int, classVisitor: ClassVisitor?) : ClassVisitor(i, classVisitor) {
    override fun visitMethod(
        i: Int,
        s: String,
        s1: String,
        s2: String,
        strings: Array<String>
    ): MethodVisitor {
        var methodVisitor = cv.visitMethod(i, s, s1, s2, strings)
        methodVisitor = object : AdviceAdapter(ASM5, methodVisitor, i, s, s1) {
            var inject = true
            override fun visitAnnotation(s: String, b: Boolean): AnnotationVisitor {
                return super.visitAnnotation(s, b)
            }

            override fun onMethodEnter() {
                //方法进入时期
                if (inject) {
                    //这里就是之前使用ASM插件生成的统计时间代码
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    mv.visitLdcInsn("this is asm input")
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Ljava/lang/String;)V",
                        false
                    )
                    mv.visitTypeInsn(NEW, "java/lang/Throwable")
                    mv.visitInsn(DUP)
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Throwable", "<init>", "()V", false)
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/lang/Throwable",
                        "getStackTrace",
                        "()[Ljava/lang/StackTraceElement;",
                        false
                    )
                    mv.visitInsn(ICONST_1)
                    mv.visitInsn(AALOAD)
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/lang/StackTraceElement",
                        "getMethodName",
                        "()Ljava/lang/String;",
                        false
                    )
                    mv.visitVarInsn(ASTORE, 1)
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false)
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "com/liuhc/testplugin/TimeManager",
                        "addStartTime",
                        "(Ljava/lang/String;J)V",
                        false
                    )
                }
            }

            override fun onMethodExit(i: Int) {
                //方法结束时期
                if (inject) {
                    //计算方法耗时
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false)
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "com/liuhc/testplugin/TimeManager",
                        "addEndTime",
                        "(Ljava/lang/String;J)V",
                        false
                    )
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "com/liuhc/testplugin/TimeManager",
                        "calcuteTime",
                        "(Ljava/lang/String;)V",
                        false
                    )
                }
            }
        }
        return methodVisitor
    }
}