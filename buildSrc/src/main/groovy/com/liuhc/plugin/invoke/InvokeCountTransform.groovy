package com.liuhc.plugin.invoke

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

public class InvokeCountTransform extends Transform {

    private InvokeCountConfig config

    InvokeCountTransform(Project project) {
        //添加自定义扩展
        config = project.extensions.create("InvokeCountConfig", InvokeCountConfig)
        config.init()
    }

    @Override
    String getName() {
        //task名字
        return "InfoTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        transformInvocation.inputs.each {
            it.directoryInputs.each {
                if (it.file.isDirectory()) {
                    it.file.eachFileRecurse {
                        def fileName = it.name
                        if (fileName.endsWith(".class") && !fileName.startsWith("R\$")
                                && fileName != "BuildConfig.class" && fileName != "R.class") {
                            //各种过滤类，关联classVisitor
                            modifyClass(it.bytes, it.name.replace(".class", ""))
                        }
                    }
                }
                def dest = transformInvocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(it.file, dest)
            }
            it.jarInputs.each { jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    /**
     * 真正修改类中方法字节码
     */
    private byte[] modifyClass(byte[] srcClass, String className) {
        try {
            ClassReader cr = new ClassReader(srcClass)
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            ClassVisitor classVisitor = new InvokeCountClassVisitor(Opcodes.ASM5, cw, config, className)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            return cw.toByteArray()
        } catch (Exception e) {
            e.printStackTrace()
            return srcClass
        }
    }
}