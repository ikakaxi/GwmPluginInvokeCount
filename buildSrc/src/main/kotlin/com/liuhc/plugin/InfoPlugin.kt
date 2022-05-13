package com.liuhc.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 描述:
 * 作者:liuhaichao
 * 创建日期：2022/5/13 on 4:53 下午
 */
internal class InfoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("abcd")
    }
}