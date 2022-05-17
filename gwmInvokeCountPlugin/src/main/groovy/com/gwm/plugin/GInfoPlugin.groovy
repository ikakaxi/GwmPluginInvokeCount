package com.gwm.plugin

import com.android.build.gradle.AppExtension
import com.gwm.plugin.invoke.InvokeCountTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

public class GInfoPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def android = project.extensions.findByType(AppExtension)
        //插件为单独项目并且使用不需要本地仓库的方式时，需要对android判空，并且AppExtension也必须要用findByType查找
        if (android != null) {
            android.registerTransform(new InvokeCountTransform(project))
        }
    }
}