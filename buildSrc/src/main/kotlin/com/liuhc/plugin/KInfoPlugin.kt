package com.liuhc.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension


/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/13 on 4:53 下午
 */
internal class KInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(InfoTransform())
    }
}