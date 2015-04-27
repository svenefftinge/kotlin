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

package org.jetbrains.idl2k

import java.util.*

private val typeMapper = mapOf(
        "unsignedlong" to "Int",
        "unsignedlonglong" to "Long",
        "longlong" to "Long",
        "unsignedshort" to "Short",
        "void" to "Unit",
        "DOMString" to "String",
        "boolean" to "Boolean",
        "short" to "Short",
        "long" to "Int",
        "double" to "Double",
        "any" to "Any",
        "" to "dynamic",
        "DOMTimeStamp" to "Number",
        "EventHandler" to "() -> Unit",
        "object" to "dynamic",
        "WindowProxy" to "Window",
        "Uint8ClampedArray" to "dynamic", // TODO think of native arrays,
        "Function" to "() -> dynamic",
        "USVString" to "String",
        "ByteString" to "String",
        "DOMError" to "dynamic",
        "SVGMatrix" to "dynamic",
        "ArrayBuffer" to "dynamic",
        "Elements" to "dynamic"
)

private fun mapType(repository: Repository, type: String) = handleSpecialTypes(repository, typeMapper[type] ?: type)

private fun handleSpecialTypes(repository: Repository, type: String): String {
    if (type == "dynamic?") {
        return "dynamic"
    } else if (type.endsWith("?")) {
        return mapType(repository, type.substring(0, type.length() - 1)) + "?"
    } else if (type.endsWith("...")) {
        return mapType(repository, type.substring(0, type.length() - 3))
    } else if (type.endsWith("[]")) {
        return "Array<${mapType(repository, type.substring(0, type.length() - 2))}>"
    } else if (type.startsWith("unrestricted")) {
        return mapType(repository, type.substring(12))
    } else if (type.startsWith("sequence")) {
        return "Any" // TODO how do we handle sequences?
    } else if (type in repository.typeDefs) {
        return "dynamic"
    } else if (type in repository.enums) {
        return "String"
    } else if (type.endsWith("Callback")) {
        return "() -> Unit"
    } else if (type.startsWith("Union<")) {
        return "dynamic"
    } else if (type.startsWith("Promise<")) {
        return "dynamic"
    } else if ("NoInterfaceObject" in repository.interfaces[type]?.extendedAttributes?.map {it.call} ?: emptyList()) {
        return "dynamic"
    }

    return type
}

private fun guessValueForType(type : String) =
    if (type.endsWith("?") || type == "dynamic") "null"
    else if (type in listOf("Int", "Short", "Long")) "0"
    else if (type == "String") "\"\""
    else "noImpl"

private fun generateInterfaceSuperTypeCall(repository: Repository, superType: InterfaceDefinition, constructor: ExtendedAttribute?): String {
    val superTypeEntity = resolveEntityType(repository, superType)

    if (superTypeEntity == "trait") {
        return superType.name
    }

    val superTypeParameters = findConstructorAttribute(superType)?.arguments ?: emptyList()
    val existingParameters = constructor?.arguments?.map { it.name }?.toSet() ?: emptySet()

    val superTypeParameterValues = superTypeParameters.map {
        if (it.name in existingParameters) {
            it.name
        } else it.defaultValue ?: guessValueForType(mapType(repository, it.type))
    }

    return superTypeParameterValues.joinToString(", ", superType.name + "(", ")")
}

fun collectSuperTypes(repository : Repository, root : InterfaceDefinition) : List<InterfaceDefinition> = HashSet<InterfaceDefinition>().let {result ->
    collectSuperTypesImpl(repository, result, listOf(root))
    result.toList()
}

tailRecursive
fun collectSuperTypesImpl(repository : Repository, result : MutableSet<InterfaceDefinition>, root : List<InterfaceDefinition>) {
    if (root.isNotEmpty()) {
        val superTraits = root.flatMap { it.superTypes }.map { repository.interfaces[it] }.filterNotNull().filter { it !in result }
        result.addAll(superTraits)
        collectSuperTypesImpl(repository, result, superTraits)
    }
}

fun <O : Appendable> O.generateInterface(repository: Repository, interface: InterfaceDefinition) {
    val constructor = findConstructorAttribute(interface)
    val entityType = resolveEntityType(repository, interface, constructor)
    val constructorPart = if (constructor != null && constructor.arguments.isNotEmpty()) "(${generateMethodParametersPart(repository, constructor.arguments)})" else ""
    val superTypes = interface.superTypes.map { repository.interfaces[it] }.filterNotNull()

    if (entityType == "class") {
        append("open ")
    }
    appendln("native ${entityType} ${interface.name}${constructorPart}${if (interface.superTypes.isNotEmpty()) " : " else ""}${superTypes.map {generateInterfaceSuperTypeCall(repository, it, constructor) }.joinToString(", ")} {")

    generateInterfaceBody(repository, interface)

    appendln("}")
    appendln()
}

private fun resolveEntityType(repository : Repository, iface: InterfaceDefinition, constructor: ExtendedAttribute? = findConstructorAttribute(iface)) : String =
        if (iface.dictionary || constructor != null || iface.superTypes.map {repository.interfaces[it]}.filterNotNull().any { superType -> resolveEntityType(repository, superType) == "class" }) "class" else "trait"

private fun findConstructorAttribute(interface: InterfaceDefinition) = interface.extendedAttributes.firstOrNull { it.call == "Constructor" }

private fun generateMethodParametersPart(repository: Repository, parameters: List<Attribute>, omitDefaultValue : Boolean = false): String {
    fun vararg(p: Attribute) = if (p.name.endsWith("...")) "vararg " else ""
    fun defaultValue(p: Attribute) = if (!omitDefaultValue && p.defaultValue != null && p.defaultValue != "") " = ${p.defaultValue}" else ""

    return parameters.map { p -> "${vararg(p)}${p.name} : ${mapType(repository, p.type)}${defaultValue(p)}" }.joinToString(", ")
}

private fun <O : Appendable> O.generateInterfaceBody(repository: Repository, interface: InterfaceDefinition) {
    val allSuperTypes = collectSuperTypes(repository, interface)
    val allAttributes = allSuperTypes.flatMap { it.attributes }.map {it.name}.toSet()
    val allOperations = allSuperTypes.flatMap { it.operations }.toSet()

    interface.attributes.filter { it.name !in allAttributes }.forEach {
        generateAttribute(repository, it, omitNoImpl = interface.dictionary)
    }

    interface.operations.filter { it !in allOperations }.forEach {
        generateOperation(repository, it)
    }

    if (interface.constants.isNotEmpty()) {
        appendln("    companion object {")

        interface.constants.forEach {
            appendln("        val ${it.name} : ${mapType(repository, it.type)} = ${it.value}")
        }

        appendln("    }")
    }

    appendln()

    val extensionInterfaces = repository.externals[interface.name]
            ?.filter { "NoInterfaceObject" in (repository.interfaces[it]?.extendedAttributes?.map { it.call } ?: emptyList()) }
            ?.map { repository.interfaces[it]!! }
            ?: emptyList()

    extensionInterfaces.flatMapTo(HashSet<Attribute>()) {it.attributes}.filter {it !in interface.attributes && it !in allAttributes}.forEach {
        generateAttribute(repository, it, omitNoImpl = interface.dictionary)
    }

    extensionInterfaces.flatMapTo(HashSet<Operation>()) {it.operations}.filter {it !in interface.operations && it !in allOperations}.forEach {
        generateOperation(repository, it)
    }
}

private fun <O : Appendable> O.generateAttribute(repository: Repository, it: Attribute, omitNoImpl : Boolean) {
    val valOrVar = if (it.readOnly) "val" else "var"
    val initializer = if (it.defaultValue != null) " = ${it.defaultValue}" else ""
    appendln("    ${valOrVar} ${it.name} : ${mapType(repository, it.type)}$initializer")
    if (!omitNoImpl) {
        appendln("        get() = noImpl")
        if (!it.readOnly) {
            appendln("        set(value) = noImpl")
        }
    }
}

private fun <O : Appendable> O.generateOperation(repository: Repository, it: Operation, omitDefaultValues : Boolean = false) {
    val getter = "getter" in it.attributes.map { it.call }
    val setter = "setter" in it.attributes.map { it.call }

    val returnType = mapType(repository, it.returnType).let { type -> if (getter && !type.endsWith("?")) "$type?" else type }

    if (it.name != "" || getter || setter) {
        if (getter) {
            appendln("    nativeGetter")
        } else if (setter) {
            appendln("    nativeSetter")
        }

        if (getter) {
            appendln("    fun get(${generateMethodParametersPart(repository, it.parameters, omitDefaultValues)}) : ${returnType} = noImpl")
        }
        if (setter) {
            appendln("    fun set(${generateMethodParametersPart(repository, it.parameters, omitDefaultValues)}) : ${returnType} = noImpl")
        }
        if (it.name != "") {
            appendln("    fun ${it.name}(${generateMethodParametersPart(repository, it.parameters, omitDefaultValues)}) : ${returnType} = noImpl")
        }
    }
}
