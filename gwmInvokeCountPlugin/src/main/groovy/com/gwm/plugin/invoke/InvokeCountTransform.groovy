package com.gwm.plugin.invoke

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.gwm.plugin.ClassNameAnalytics
import com.gwm.plugin.TransformHelper
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.ByteArrayOutputStream
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.concurrent.Callable
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

public class InvokeCountTransform extends Transform {
    private WaitableExecutor waitableExecutor
    private TransformHelper transformHelper
    private InvokeCountConfig config
    boolean print = true

    InvokeCountTransform(Project project) {
        waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        transformHelper = new TransformHelper()
        //添加自定义扩展
        config = project.extensions.create("InvokeCountConfig", InvokeCountConfig)
        print = true
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
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        if (!transformInvocation.isIncremental()) {
            transformInvocation.outputProvider.deleteAll()
        }

        //遍历输入文件
        transformInvocation.inputs.each { TransformInput input ->
            //遍历 jar
            input.jarInputs.each { JarInput jarInput ->
                if (waitableExecutor) {
                    waitableExecutor.execute(new Callable<Object>() {
                        @Override
                        Object call() throws Exception {
                            forEachJar(transformInvocation.isIncremental(), jarInput, transformInvocation.outputProvider, transformInvocation.context)
                            return null
                        }
                    })
                } else {
                    forEachJar(transformInvocation.isIncremental(), jarInput, transformInvocation.outputProvider, transformInvocation.context)
                }
            }

            //遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (waitableExecutor) {
                    waitableExecutor.execute(new Callable<Object>() {
                        @Override
                        Object call() throws Exception {
                            forEachDirectory(transformInvocation.isIncremental(), directoryInput, transformInvocation.outputProvider, transformInvocation.context)
                            return null
                        }
                    })
                } else {
                    forEachDirectory(transformInvocation.isIncremental(), directoryInput, transformInvocation.outputProvider, transformInvocation.context)
                }
            }
        }
        if (waitableExecutor) {
            waitableExecutor.waitForTasksWithQuickFail(true)
        }
    }
//-------------------------------------------------------------------------------------------------
// jar扫描
    void forEachJar(boolean isIncremental, JarInput jarInput, TransformOutputProvider outputProvider, Context context) {
        String destName = jarInput.file.name
        //截取文件路径的 md5 值重命名输出文件，因为可能同名，会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        //获得输出文件
        File destFile = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            Status status = jarInput.getStatus()
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    println("jar status = $status:$destFile.absolutePath")
                    transformJar(destFile, jarInput, context)
                    break
                case Status.REMOVED:
                    println("jar status = $status:$destFile.absolutePath")
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    break
                default:
                    break
            }
        } else {
            transformJar(destFile, jarInput, context)
        }
    }

    void transformJar(File dest, JarInput jarInput, Context context) {
        def modifiedJar = modifyJarFile(jarInput.file, context.getTemporaryDir())
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }
        FileUtils.copyFile(modifiedJar, dest)
    }

    /**
     * 修改 jar 文件中对应字节码
     */
    private File modifyJarFile(File jarFile, File tempDir) {
        if (jarFile) {
            return modifyJar(jarFile, tempDir, true)

        }
        return null
    }

    private File modifyJar(File jarFile, File tempDir, boolean isNameHex) {
        //FIX: ZipException: zip file is empty
        if (jarFile == null || jarFile.length() == 0) {
            return null
        }
        //取原 jar, verify 参数传 false, 代表对 jar 包不进行签名校验
        def file = new JarFile(jarFile, false)
        //设置输出到的 jar
        def tmpNameHex = ""
        if (isNameHex) {
            tmpNameHex = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, tmpNameHex + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                IOUtils.closeQuietly(inputStream)
                e.printStackTrace()
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                JarEntry entry = new JarEntry(entryName)
                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes
                try {
                    jarOutputStream.putNextEntry(entry)
                    sourceClassBytes = toByteArrayAndAutoCloseStream(inputStream)
                } catch (Exception e) {
                    println("Exception encountered while processing jar: " + jarFile.getAbsolutePath())
                    IOUtils.closeQuietly(file)
                    IOUtils.closeQuietly(jarOutputStream)
                    e.printStackTrace()
                    return null
                }
                if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                    className = entryName.replace("/", ".").replace(".class", "")
                    ClassNameAnalytics classNameAnalytics = transformHelper.analytics(className)
                    if (classNameAnalytics.isShouldModify) {
                        modifiedClassBytes = modifyClass(sourceClassBytes, className)
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes)
                } else {
                    jarOutputStream.write(modifiedClassBytes)
                }
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

//-------------------------------------------------------------------------------------------------
//    目录扫描
    void forEachDirectory(boolean isIncremental, DirectoryInput directoryInput, TransformOutputProvider outputProvider, Context context) {
        File dir = directoryInput.file
        File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY)
        FileUtils.forceMkdir(dest)
        String srcDirPath = dir.absolutePath
        String destDirPath = dest.absolutePath
        if (isIncremental) {
            Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
            for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                Status status = changedFile.getValue()
                File inputFile = changedFile.getKey()
                String destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
                File destFile = new File(destFilePath)
                switch (status) {
                    case Status.NOTCHANGED:
                        break
                    case Status.REMOVED:
                        println("目录 status = $status:$inputFile.absolutePath")
                        if (destFile.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            destFile.delete()
                        }
                        break
                    case Status.ADDED:
                    case Status.CHANGED:
                        println("目录 status = $status:$inputFile.absolutePath")
                        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                        if (modified != null) {
                            FileUtils.copyFile(modified, destFile)
                            modified.delete()
                        } else {
                            FileUtils.copyFile(inputFile, destFile)
                        }
                        break
                    default:
                        break
                }
            }
        } else {
            FileUtils.copyDirectory(dir, dest)
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File inputFile ->
                    forEachDir(dir, inputFile, context, srcDirPath, destDirPath)
            }
        }
    }

    void forEachDir(File dir, File inputFile, Context context, String srcDirPath, String destDirPath) {
        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
        if (modified != null) {
            File target = new File(inputFile.absolutePath.replace(srcDirPath, destDirPath))
            if (target.exists()) {
                target.delete()
            }
            FileUtils.copyFile(modified, target)
            modified.delete()
        }
    }

    /**
     * 目录文件中修改对应字节码
     */
    private File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        FileOutputStream outputStream = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            ClassNameAnalytics classNameAnalytics = transformHelper.analytics(className)
            if (classNameAnalytics.isShouldModify) {
                byte[] sourceClassBytes = toByteArrayAndAutoCloseStream(new FileInputStream(classFile))
                byte[] modifiedClassBytes = modifyClass(sourceClassBytes, className)
                if (modifiedClassBytes) {
                    modified = new File(tempDir, UUID.randomUUID().toString() + '.class')
                    if (modified.exists()) {
                        modified.delete()
                    }
                    modified.createNewFile()
                    outputStream = new FileOutputStream(modified)
                    outputStream.write(modifiedClassBytes)
                }
            } else {
                return classFile
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return modified
    }

//-------------------------------------------------------------------------------------------------
    /**
     * 公用方法
     */
    private static byte[] toByteArrayAndAutoCloseStream(InputStream input) throws Exception {
        ByteArrayOutputStream output = null
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4]
            int n = 0
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n)
            }
            output.flush()
            return output.toByteArray()
        } catch (Exception e) {
            throw e
        } finally {
            IOUtils.closeQuietly(output)
            IOUtils.closeQuietly(input)
        }
    }

    private static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }
//-------------------------------------------------------------------------------------------------
    /**
     * 真正修改类中方法字节码
     */
    private byte[] modifyClass(byte[] srcClass, String className) {
        if (print) {
            print = false
            println("------------------------------------------------------------------------------")
            println("------------------------------------------------------------------------------")
            println("------------------------------开始查找要插桩的类----------------------------------")
            println("------------------------------------------------------------------------------")
            println("------------------------------------------------------------------------------")
        }
        if (config != null && config.containClass(className)) {
            println("-----------------------找到了:" + className + "-----------------------")
        }
        try {
            ClassReader cr = new ClassReader(srcClass)
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            ClassVisitor classVisitor = new InvokeCountClassVisitor(Opcodes.ASM6, cw, config, className)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            return cw.toByteArray()
        } catch (Exception e) {
            e.printStackTrace()
            return srcClass
        }
    }
}