package com.ronin.grace.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.ronin.grace.transform.GraceTransform
import org.gradle.api.Plugin
import org.gradle.api.Project


class GracePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("aop plugin ~~~~~")
        project.extensions.create("plugin_config", Params)
        def android = project.extensions.getByType(AppExtension)
        registerTransform(android)
        project.afterEvaluate {
            Params params = project.plugin_config
            println("param name:" + params.getName())
            println("param isDebug:" + params.isDebug)
            println("param data:" + params.getData())
        }


    }

    def static registerTransform(BaseExtension android) {
        GraceTransform autoTransform = new GraceTransform()
        android.registerTransform(autoTransform)

    }

}