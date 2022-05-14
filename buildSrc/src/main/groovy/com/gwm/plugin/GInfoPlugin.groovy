package com.gwm.plugin

import com.android.build.gradle.AppExtension
import com.gwm.plugin.invoke.InvokeCountTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

public class GInfoPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new InvokeCountTransform(project))
    }
}