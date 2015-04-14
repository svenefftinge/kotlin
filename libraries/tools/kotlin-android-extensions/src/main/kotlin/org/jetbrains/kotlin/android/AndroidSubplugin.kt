/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.android

import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.gradle.api
import com.android.build.gradle.BaseExtension
import java.io.File
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class AndroidSubplugin : KotlinGradleSubplugin {

    override fun getExtraArguments(project: api.Project, task: AbstractCompile): List<SubpluginOption>? {
        val androidExtension = project.getExtensions().getByName("android") as? BaseExtension

        if (androidExtension == null) return null

        val sourceSets = androidExtension.getSourceSets()
        val mainSourceSet = sourceSets.getByName("main")

        val resourceDirs = mainSourceSet.getRes().getSrcDirs()
        val manifestFile = mainSourceSet.getManifest().getSrcFile()

        val compileDependencies = project.getConfigurations().getByName("compile").getFiles()
        val supportV4 = compileDependencies?.filter { it.name.startsWith("support-v4-") }?.isNotEmpty() ?: false
        val supportV4Property = if (supportV4) "true" else "false"

        if (resourceDirs.isNotEmpty()) {
            val resourceDirOptions = resourceDirs.map { resourceDir ->
                resourceDir.listFiles { it.isDirectory() && it.name.startsWith("layout") }?.forEach { task.source(it) }
                SubpluginOption("androidRes", resourceDir.getAbsolutePath())
            }
            return listOf(
                    SubpluginOption("androidManifest", manifestFile.getAbsolutePath()),
                    SubpluginOption("supportV4", supportV4Property)
            ) + resourceDirOptions
        }

        return null
    }

    override fun getPluginName(): String {
        return "org.jetbrains.kotlin.android"
    }

    override fun getGroupName(): String {
        return "org.jetbrains.kotlin"
    }

    override fun getArtifactName(): String {
        return "kotlin-android-extensions"
    }
}