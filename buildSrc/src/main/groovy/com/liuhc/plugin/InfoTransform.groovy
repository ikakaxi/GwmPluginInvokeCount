package com.liuhc.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

public class InfoTransform extends Transform{
    @Override
    String getName() {
        //task名字
        return "InfoTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
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
                        println("-------------" + fileName + "---------------")
                        if (fileName.endsWith(".class") && !fileName.startsWith("R\$")
                                && fileName != "BuildConfig.class" && fileName != "R.class") {
                            //各种过滤类，关联classVisitor
                            handleFile(it)
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

    private static void handleFile(File file) {
        def cr = new ClassReader(file.bytes)
        def cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        def classVisitor = new MethodTotal(Opcodes.ASM5, cw)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        def bytes = cw.toByteArray()
        //写回原来这个类所在的路径
        FileOutputStream fos = new FileOutputStream(file.getParentFile().getAbsolutePath() + File.separator + file.name)
        fos.write(bytes)
        fos.close()
    }
}