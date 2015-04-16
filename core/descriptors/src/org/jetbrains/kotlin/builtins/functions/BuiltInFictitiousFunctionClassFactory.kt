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

import org.jetbrains.kotlin.builtins.KOTLIN_REFLECT_FQ_NAME
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.ClassDescriptorFactory
import org.jetbrains.kotlin.storage.StorageManager
import java.util.ArrayList
import java.util.EnumSet

/**
 * Produces [ClassDescriptor]s for the following numbered function types, with superclasses:
 *
 * Function1 : Function
 * KFunction1 : Function1, KFunction
 * KMemberFunction1 : Function2, KMemberFunction
 * KExtensionFunction1 : Function2, KExtensionFunction
 * (TODO) KMemberExtensionFunction1 : Function3, KMemberExtensionFunction
*/
public class BuiltInFictitiousFunctionClassFactory(
        protected val storageManager: StorageManager,
        private val module: ModuleDescriptor
) : ClassDescriptorFactory {

    private enum class Kind(val classNamePrefix: String) {
        Function : Kind("Function")
        KFunction : Kind("KFunction")
        KMemberFunction : Kind("KMemberFunction")
        KExtensionFunction : Kind("KExtensionFunction")
        // TODO: KMemberExtensionFunction

        fun numberedClassName(arity: Int) = Name.identifier("$classNamePrefix$arity")
        fun hasDispatchReceiver() = this == KMemberFunction
        fun hasExtensionReceiver() = this == KExtensionFunction
    }

    private object Kinds {
        val Functions = EnumSet.of(Kind.Function)
        val KFunctions = EnumSet.of(Kind.KFunction, Kind.KMemberFunction, Kind.KExtensionFunction)
    }

    private data class KindWithArity(val kind: Kind, val arity: Int)

    private fun parseClassName(className: String, allowedKinds: Set<Kind>): KindWithArity? {
        for (kind in allowedKinds) {
            val prefix = kind.classNamePrefix
            if (!className.startsWith(prefix)) continue

            val arityString = className.substring(prefix.length())
            // We shouldn't find FunctionN for large N (greater than ~255)
            if (arityString.length() > 3) continue

            val arity = try { arityString.toInt() } catch (e: NumberFormatException) { continue }

            // TODO: validate arity, should be <= 255 for functions, <= 254 for members/extensions
            return KindWithArity(kind, arity)
        }

        return null
    }

    private fun computeSuperClasses(packageFragment: PackageFragmentDescriptor, kind: Kind, arity: Int): List<ClassDescriptor> {
        val superClasses = ArrayList<ClassDescriptor>(2)

        fun add(packageFragment: PackageFragmentDescriptor, name: Name) {
            val descriptor = packageFragment.getMemberScope().getClassifier(name) as? ClassDescriptor
                             ?: error("Class $name not found in $packageFragment")
            superClasses.add(descriptor)
        }

        add(packageFragment, Name.identifier(kind.classNamePrefix))

        if (kind in Kinds.KFunctions) {
            val functionArity = when (kind) {
                Kind.KFunction -> arity
                Kind.KMemberFunction, Kind.KExtensionFunction -> arity + 1
                else -> error(kind)
            }

            val kotlinPackageFragment = module.getPackageFragmentProvider().getPackageFragments(BUILT_INS_PACKAGE_FQ_NAME).single()
            add(kotlinPackageFragment, Kind.Function.numberedClassName(functionArity))
        }

        return superClasses
    }

    override fun createClass(classId: ClassId): ClassDescriptor? {
        if (classId.isLocal() || classId.isNestedClass()) return null

        val className = classId.getRelativeClassName().asString()
        if ("Function" !in className) return null // An optimization

        val packageFqName = classId.getPackageFqName()

        val allowedKinds = when (packageFqName) {
            BUILT_INS_PACKAGE_FQ_NAME -> Kinds.Functions
            KOTLIN_REFLECT_FQ_NAME -> Kinds.KFunctions
            else -> return null
        }

        val kindWithArity = parseClassName(className, allowedKinds) ?: return null
        val (kind, arity) = kindWithArity // KT-5100

        // TODO: assertion message, cache maybe?
        val containingPackageFragment = module.getPackageFragmentProvider().getPackageFragments(packageFqName).single()

        val superClasses = computeSuperClasses(containingPackageFragment, kind, arity)

        return FunctionClassDescriptor(storageManager, containingPackageFragment, arity, kind.numberedClassName(arity), superClasses,
                                       kind.hasDispatchReceiver(), kind.hasExtensionReceiver())
    }
}
