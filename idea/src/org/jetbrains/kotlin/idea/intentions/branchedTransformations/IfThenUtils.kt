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

package org.jetbrains.kotlin.idea.intentions.branchedTransformations

import org.jetbrains.kotlin.lexer.JetTokens
import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.idea.refactoring.introduce.introduceVariable.KotlinIntroduceVariableHandler
import org.jetbrains.kotlin.resolve.BindingContext
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.refactoring.inline.KotlinInlineValHandler
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.search.LocalSearchScope
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsStatement
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.*

val NULL_PTR_EXCEPTION = "NullPointerException"
val NULL_PTR_EXCEPTION_FQ = "java.lang.NullPointerException"
val KOTLIN_NULL_PTR_EXCEPTION = "KotlinNullPointerException"
val KOTLIN_NULL_PTR_EXCEPTION_FQ = "kotlin.KotlinNullPointerException"

fun JetBinaryExpression.comparesNonNullToNull(): Boolean {
    val operationToken = this.getOperationToken()
    val rhs = this.getRight()
    val lhs = this.getLeft()
    if (rhs == null || lhs == null) return false

    val rightIsNull = rhs.isNullExpression()
    val leftIsNull = lhs.isNullExpression()
    return leftIsNull != rightIsNull && (operationToken == JetTokens.EQEQ || operationToken == JetTokens.EXCLEQ)
}

fun JetExpression.extractExpressionIfSingle(): JetExpression? {
    val innerExpression = JetPsiUtil.deparenthesize(this)
    if (innerExpression is JetBlockExpression) {
        return if (innerExpression.getStatements().size() == 1)
            JetPsiUtil.deparenthesize(innerExpression.getStatements().firstOrNull() as? JetExpression)
        else
            null
    }

    return innerExpression
}

fun JetExpression.isStatement(): Boolean = isUsedAsStatement(this.analyze())

fun JetBinaryExpression.getNonNullExpression(): JetExpression? = when {
    this.getLeft()?.isNullExpression() == false ->
        this.getLeft()
    this.getRight()?.isNullExpression() == false ->
        this.getRight()
    else ->
        null
}

fun JetExpression.isNullExpression(): Boolean = this.extractExpressionIfSingle()?.getText() == "null"

fun JetExpression.isNullExpressionOrEmptyBlock(): Boolean = this.isNullExpression() || this is JetBlockExpression && this.getStatements().isEmpty()

fun JetExpression.isThrowExpression(): Boolean = this.extractExpressionIfSingle() is JetThrowExpression

fun JetThrowExpression.throwsNullPointerExceptionWithNoArguments(): Boolean {
    val thrownExpression = this.getThrownExpression()
    if (thrownExpression !is JetCallExpression) return false

    val context = this.analyze()
    val descriptor = context.get(BindingContext.REFERENCE_TARGET, thrownExpression.getCalleeExpression() as JetSimpleNameExpression)
    val declDescriptor = descriptor?.getContainingDeclaration()
    if (declDescriptor == null) return false

    val exceptionName = DescriptorUtils.getFqName(declDescriptor).asString()
    return (exceptionName == NULL_PTR_EXCEPTION_FQ || exceptionName == KOTLIN_NULL_PTR_EXCEPTION_FQ) && thrownExpression.getValueArguments().isEmpty()
}

fun JetExpression.isNotNullExpression(): Boolean {
    val innerExpression = this.extractExpressionIfSingle()
    return innerExpression != null && innerExpression.getText() != "null"
}

fun JetExpression.evaluatesTo(other: JetExpression): Boolean {
    return this.extractExpressionIfSingle()?.getText() == other.getText()
}

fun JetExpression.convertToIfNotNullExpression(conditionLhs: JetExpression, thenClause: JetExpression, elseClause: JetExpression?): JetIfExpression {
    val condition = JetPsiFactory(this).createExpression("${conditionLhs.getText()} != null")
    return this.convertToIfStatement(condition, thenClause, elseClause)
}

fun JetExpression.convertToIfNullExpression(conditionLhs: JetExpression, thenClause: JetExpression): JetIfExpression {
    val condition = JetPsiFactory(this).createExpression("${conditionLhs.getText()} == null")
    return this.convertToIfStatement(condition, thenClause, null)
}

fun JetExpression.convertToIfStatement(condition: JetExpression, thenClause: JetExpression, elseClause: JetExpression?): JetIfExpression {
    val elseBranch = if (elseClause == null) "" else " else ${elseClause.getText()}"
    val conditionalString = "if (${condition.getText()}) ${thenClause.getText()}$elseBranch"

    val st = this.replace(conditionalString) as JetExpression
    return JetPsiUtil.deparenthesize(st) as JetIfExpression
}

fun JetIfExpression.introduceValueForCondition(occurrenceInThenClause: JetExpression, editor: Editor) {
    val project = this.getProject()
    val occurrenceInConditional = (this.getCondition() as JetBinaryExpression).getLeft()!!
    KotlinIntroduceVariableHandler.doRefactoring(project,
                                                 editor,
                                                 occurrenceInConditional,
                                                 listOf(occurrenceInConditional, occurrenceInThenClause),
                                                 null)
}

fun JetElement.replace(expressionAsString: String): PsiElement =
        this.replace(JetPsiFactory(this).createExpression(expressionAsString))

fun JetSimpleNameExpression.inlineIfDeclaredLocallyAndOnlyUsedOnceWithPrompt(editor: Editor) {
    val declaration = this.getReference()?.resolve() as JetDeclaration

    if (declaration !is JetProperty) return

    val enclosingElement = JetPsiUtil.getEnclosingElementForLocalDeclaration(declaration)
    val isLocal = enclosingElement != null
    if (!isLocal) return

    val scope = LocalSearchScope(enclosingElement!!)

    val references = ReferencesSearch.search(declaration, scope).findAll()
    if (references.size() == 1) {
        KotlinInlineValHandler().inlineElement(this.getProject(), editor, declaration)
    }
}

fun JetSafeQualifiedExpression.inlineReceiverIfApplicableWithPrompt(editor: Editor) {
    (this.getReceiverExpression() as? JetSimpleNameExpression)?.inlineIfDeclaredLocallyAndOnlyUsedOnceWithPrompt(editor)
}

fun JetBinaryExpression.inlineLeftSideIfApplicableWithPrompt(editor: Editor) {
    (this.getLeft() as? JetSimpleNameExpression)?.inlineIfDeclaredLocallyAndOnlyUsedOnceWithPrompt(editor)
}

fun JetPostfixExpression.inlineBaseExpressionIfApplicableWithPrompt(editor: Editor) {
    (this.getBaseExpression() as? JetSimpleNameExpression)?.inlineIfDeclaredLocallyAndOnlyUsedOnceWithPrompt(editor)
}

fun JetExpression.isStableVariable(): Boolean {
    val context = this.analyze()
    val descriptor = BindingContextUtils.extractVariableDescriptorIfAny(context, this, false)
    return descriptor is VariableDescriptor &&
           DataFlowValueFactory.isStableVariable(descriptor, DescriptorUtils.getContainingModule(descriptor))
}
