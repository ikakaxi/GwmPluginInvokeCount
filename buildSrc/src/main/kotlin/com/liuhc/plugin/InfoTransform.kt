package com.liuhc.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/13 on 9:38 下午
 */
class InfoTransform : Transform() {

    override fun transform(transformInvocation: TransformInvocation) {
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                if (directoryInput.file.isDirectory) {
                    directoryInput.file.listFiles()?.forEach { file ->
                        println("-----------------------${file.name}-------------------------")
                        val fileName = file.name
                        if (fileName.endsWith(".class") && !fileName.startsWith("R\$")
                            && fileName != "BuildConfig.class" && fileName != "R.class"
                        ) {
                            //各种过滤类，关联classVisitor
                            handleFile(file)
                        }
                    }
                }
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            transformInput.jarInputs.forEach { jarInput ->
                var jarName = jarInput.name
                val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length - 4)
                }
                val dest = transformInvocation.outputProvider.getContentLocation(
                    jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

    }

    override fun getName(): String {
        return "InfoTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    private fun handleFile(file: File) {
        val cr = ClassReader(file.readBytes())
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        val classVisitor = MethodTotal(Opcodes.ASM5, cw)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        val bytes = cw.toByteArray()
        //写回原来这个类所在的路径
        val fos =
            FileOutputStream(file.parentFile.absolutePath + File.separator + file.name)
        fos.write(bytes)
        fos.close()
    }

}