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

package org.jetbrains.kotlin.builtins.functions

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AbstractClassDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.scopes.StaticScopeForKotlinClass
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.utils.toReadOnlyList
import java.util.ArrayList

class FunctionClassDescriptor(
        private val storageManager: StorageManager,
        private val containingDeclaration: PackageFragmentDescriptor,
        val arity: Int,
        private val className: Name,
        private val superClasses: List<ClassDescriptor>,
        private val hasDispatchReceiver: Boolean,
        private val hasExtensionReceiver: Boolean
) : AbstractClassDescriptor(storageManager, className) {
    private val staticScope = StaticScopeForKotlinClass(this)
    private val typeConstructor = FunctionTypeConstructor()
    private val memberScope = FunctionClassScope(storageManager, this)

    override fun getContainingDeclaration() = containingDeclaration

    override fun getStaticScope() = staticScope

    override fun getTypeConstructor(): TypeConstructor = typeConstructor

    override fun getScopeForMemberLookup() = memberScope

    override fun getCompanionObjectDescriptor() = null
    override fun getConstructors() = emptyList<ConstructorDescriptor>()
    override fun getKind() = ClassKind.TRAIT
    override fun getModality() = Modality.ABSTRACT
    override fun getUnsubstitutedPrimaryConstructor() = null
    override fun getVisibility() = Visibilities.PUBLIC
    override fun isCompanionObject() = false
    override fun isInner() = false
    override fun getAnnotations() = Annotations.EMPTY
    override fun getSource() = SourceElement.NO_SOURCE

    private inner class FunctionTypeConstructor : AbstractClassTypeConstructor() {
        private val parameters = storageManager.createLazyValue {
            val result = ArrayList<TypeParameterDescriptor>()

            fun typeParameter(variance: Variance, name: String) {
                result.add(TypeParameterDescriptorImpl.createWithDefaultBound(
                        this@FunctionClassDescriptor, Annotations.EMPTY, false, variance, Name.identifier(name), result.size()
                ))
            }

            if (hasDispatchReceiver) {
                typeParameter(Variance.IN_VARIANCE, "T")
            }
            if (hasExtensionReceiver) {
                typeParameter(Variance.IN_VARIANCE, "E")
            }

            (1..arity).map { i ->
                typeParameter(Variance.IN_VARIANCE, "P$i")
            }

            typeParameter(Variance.OUT_VARIANCE, "R")

            result.toReadOnlyList()
        }

        override fun getParameters() = parameters()

        override fun getSupertypes(): Collection<JetType> = superClasses.map { superClass ->
            // Substitute K type parameters of the super class with our last K type parameters
            val typeConstructor = superClass.getTypeConstructor()
            val superParameters = typeConstructor.getParameters()
            val arguments = getParameters().takeLast(superParameters.size()).map { TypeProjectionImpl(it.getDefaultType()) }
            JetTypeImpl(Annotations.EMPTY, typeConstructor, false, arguments, superClass.getMemberScope(arguments))
        }

        override fun getDeclarationDescriptor() = this@FunctionClassDescriptor
        override fun isDenotable() = true
        override fun isFinal() = false
        override fun getAnnotations() = Annotations.EMPTY

        override fun toString() = getDeclarationDescriptor().toString()
    }

    override fun toString() = DescriptorUtils.getFqName(this).asString()
}
