package com.ronin.grace.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.ronin.grace.visit.GraceVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class GraceTransform extends Transform {

    @Override
    String getName() {
        return "GraceTransform"
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
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        println("||=======================================================================================================")
        println("||                                                 开始计时                                               ")
        println("||=======================================================================================================")

        if (null != outputProvider) {
            outputProvider.deleteAll()
        }

        def startTime = System.currentTimeMillis()

        inputs.each { TransformInput input ->

            //遍历directory文件
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name

                        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {

                            println(name)

                            ClassReader classReader = new ClassReader(file.bytes)
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                            ClassVisitor cv = new GraceVisitor(classWriter)
//
                            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                            byte[] code = classWriter.toByteArray()
                            FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
                            fos.write(code)
                            fos.close()
                        }
                    }
                }

                //处理完输入文件之后，要把输出给下一个任务
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)

            }

            //遍历jar文件
            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5(jarInput.file.getAbsolutePath())

                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                File temFile = null
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {

                    JarFile jarFile = new JarFile(jarInput.file)

                    temFile = new File(jarInput.file.getParent() + File.separator + "classes_ronin.jar")
                    if (temFile.exists()) {
                        temFile.delete()
                    }

                    def entries = jarFile.entries()
                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(temFile))
                    ArrayList<String> processList = new ArrayList<>()

                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement()
                        String entryName = jarEntry.name
                        ZipEntry zipEntry = new ZipEntry(entryName)
//                        println("entryName=" + entryName)
                        InputStream inputStream = jarFile.getInputStream(jarEntry)

                        //重点:插桩class
                        if (entryName.endsWith(".class") && !entryName.contains("R\$") &&
                                !entryName.contains("R.class") && !entryName.contains("BuildConfig.class")) {
                            jarOutputStream.putNextEntry(zipEntry)

                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                            ClassVisitor classVisitor = new GraceVisitor(classWriter)
                            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                            def array = classWriter.toByteArray()
                            jarOutputStream.write(array)

                        } else if (entryName.contains("META-INF/services/javax.annotation.processing.Processor")) {
                            if (!processList.contains(entryName)) {
                                processList.add(entryName)

                                jarOutputStream.putNextEntry(zipEntry)
                                jarOutputStream.write(IOUtils.toByteArray(inputStream))
                            } else {
                                println("duplicate entry:" + entryName)
                            }
                        } else {
                            jarOutputStream.putNextEntry(zipEntry)
                            jarOutputStream.write(IOUtils.toByteArray(inputStream))

                        }
                    }

                    jarOutputStream.close()
                    jarFile.close()

                }


                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (null == temFile) {
                    FileUtils.copyFile(jarInput.file, dest)
                } else {
                    FileUtils.copyFile(temFile, dest)
                    temFile.delete()
                }
            }


        }

        //计算耗时
        def cost = (System.currentTimeMillis() - startTime) / 1000
        println("||=======================================================================================================")
        println("||                                       计时结束:费时${cost}秒                                           ")
        println("||=======================================================================================================")

    }


}