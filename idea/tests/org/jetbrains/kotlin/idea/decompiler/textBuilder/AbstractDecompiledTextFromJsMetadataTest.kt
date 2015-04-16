/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.decompiler.textBuilder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.kotlin.idea.decompiler.KotlinJavascriptMetaFile
import org.jetbrains.kotlin.idea.decompiler.KotlinJavascriptMetaFileType
import org.jetbrains.kotlin.idea.decompiler.navigation.NavigateToDecompiledLibraryTest
import org.jetbrains.kotlin.idea.test.JdkAndMockLibraryProjectDescriptor
import org.jetbrains.kotlin.idea.test.JetLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.test.JetLightProjectDescriptor
import org.jetbrains.kotlin.idea.test.PluginTestCaseBase
import org.jetbrains.kotlin.psi.psiUtil.getElementTextWithContext
import org.junit.Assert

public abstract class AbstractDecompiledTextFromJsMetadataTest : JetLightCodeInsightFixtureTestCase() {

    private val TEST_DATA_PATH = PluginTestCaseBase.getTestDataPathBase() + "/decompiler/decompiledText"

    public fun doTest(path: String) {
        val classFile = getJsMetaFile("test", getTestName(false), myModule!!)!!
        val clsFile = PsiManager.getInstance(getProject()).findFile(classFile)
        Assert.assertTrue("Expecting decompiled kotlin javascript file, was: " + clsFile!!.javaClass, clsFile is KotlinJavascriptMetaFile)
        UsefulTestCase.assertSameLinesWithFile(path.substring(0, path.length() - 1) + ".expected.kt", clsFile.getText())
        checkThatFileWasParsedCorrectly(clsFile)
    }

    override fun tearDown() {
        // Copied verbatim from NavigateToStdlibSourceRegressionTest.
        // Workaround for IDEA's bug during tests.
        // After tests IDEA disposes VirtualFiles within LocalFileSystem, but doesn't rebuild indices.
        // This causes library source files to be impossible to find via indices
        super.tearDown()
        ApplicationManager.getApplication().runWriteAction(object : Runnable {
            override fun run() {
                LightPlatformTestCase.closeAndDeleteProject()
            }
        })
    }

    private fun getJsMetaFile(packageName: String, className: String, module: Module): VirtualFile? {
        val root = NavigateToDecompiledLibraryTest.findTestLibraryRoot(module)
        TestCase.assertNotNull(root)
        val moduleDirName = JdkAndMockLibraryProjectDescriptor.LIBRARY_NAME
        val packageDir = root!!.findFileByRelativePath(moduleDirName + "/" + packageName.replace(".", "/"))
        TestCase.assertNotNull(packageDir)
        val metaFile = packageDir!!.findChild(className + "." + KotlinJavascriptMetaFileType.INSTANCE.getDefaultExtension())
        TestCase.assertNotNull(metaFile)
        return metaFile
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        if (isAllFilesPresentInTest()) {
            return JetLightProjectDescriptor.INSTANCE
        }
        return JdkAndMockLibraryProjectDescriptor(TEST_DATA_PATH + "/" + getTestName(false), false, true)
    }

    private fun checkThatFileWasParsedCorrectly(clsFile: PsiFile) {
        clsFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitErrorElement(element: PsiErrorElement) {
                Assert.fail("Decompiled file should not contain error elements!\n${element.getElementTextWithContext()}")
            }
        })
    }
}
