package com.lewis.asmdemoplugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream

class DemoTransform : Transform(), Plugin<Project> {


    override fun apply(p0: Project) {
        println("=======apply ${p0.version}=======")
        p0.extensions.getByType(AppExtension::class.java)//获取扩展的类型,apk
            .registerTransform(this)//注册transform
        println("=======apply register=======")
    }

    override fun getName(): String {
        return "ASMDemoTransform"
    }

    //transform要处理的输入类型,有class,resource,dex
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    //是否为增量工作
    override fun isIncremental(): Boolean {
        return false
    }

    /**
     * 输入文件的范围
     * PROJECT 当前工程
     * SUB_PROJECTS 子工程
     * EXTERNAL_LIBRARIES lib
     * LOCAL_DEPS jar
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //具体处理
    override fun transform(transformInvocation: TransformInvocation?) {
        println("transform start")
        val inputs = transformInvocation?.inputs
        val outputProvider = transformInvocation?.outputProvider

        if (!isIncremental) {
            outputProvider?.deleteAll()
        }

        inputs?.forEach {
            it.directoryInputs.forEach {
                if (it.file.isDirectory) {
                    FileUtils.getAllFiles(it.file).forEach { file ->
                        val name = file.name
                        println("transform name :$name")
                        if (name.endsWith(".class") && name != "R.class"
                            && !name.startsWith("R\$") && name != ("BuildConfig.class")
                        ) {//过滤出需要的class,将一些基本用不到的class去掉
                            val classPath = file.absoluteFile
                            println(">>>>>>>>> classPath :$classPath")

                            val cr = ClassReader(file.readBytes())
                            val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                            val visitor = DemoClassVisitor(cw)
                            cr.accept(visitor, ClassReader.EXPAND_FRAMES)

                            val bytes = cw.toByteArray()

                            val fos = FileOutputStream(classPath)
                            fos.write(bytes)
                            fos.close()
                        }
                    }
                }

                val dest = outputProvider?.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectoryToDirectory(it.file, dest)
            }

            //将jar也加进来,androidx需要这个
            it.jarInputs.forEach {
                val dest = outputProvider?.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.JAR
                )
                FileUtils.copyFile(it.file, dest)
            }
        }
    }

}