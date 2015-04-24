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

private fun mapType(repository: Repository, type : String) = handleSpecialTypes(repository, typeMapper[type] ?: type)

private fun handleSpecialTypes(repository: Repository, type : String) : String {
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
        return "dynamic"
    } else if (type.endsWith("Callback")) {
        return "() -> Unit"
    } else if (type.startsWith("Union<")) {
        return "dynamic"
    } else if (type.startsWith("Promise<")) {
        return "dynamic"
    }

    return type
}

fun <O: Appendable> O.generateInterface(repository : Repository, interface: InterfaceDefinition) {
    appendln("native trait ${interface.name}${if (interface.superTypes.isNotEmpty()) " : " else ""}${interface.superTypes.joinToString(", ")} {")

    generateInterfaceBody(repository, interface)

    appendln("}")
    appendln()
}

private fun <O: Appendable> O.generateInterfaceBody(repository: Repository, interface: InterfaceDefinition) {
    val superTypeAttributes = interface.superTypes.flatMap { repository.interfaces[it]?.attributes ?: emptyList() }.map {it.name}.toSet()
    val superTypeOperations = interface.superTypes.flatMap { repository.interfaces[it]?.operations ?: emptyList() }.map {it.name}.toSet()

    interface.attributes.filter {it.name !in superTypeAttributes}.forEach {
        val valOrVar = if (it.readOnly) "val" else "var"
        appendln("    ${valOrVar} ${it.name} : ${mapType(repository, it.type)}")
    }

    interface.operations.filter {it.name !in superTypeOperations}.forEach {
        val getter = "getter" in it.attributes.map {it.call}
        val setter = "setter" in it.attributes.map {it.call}

        val returnType = mapType(repository, it.returnType).let { type -> if (getter && !type.endsWith("?")) "$type?" else type }

        fun vararg(p : Attribute) = if (p.name.endsWith("...")) "vararg " else ""

        if (it.name != "" || getter || setter) {
            if (getter) {
                appendln("    nativeGetter")
            } else if (setter) {
                appendln("    nativeSetter")
            }

            if (getter) {
                appendln("    fun get(${it.parameters.map { p -> "${vararg(p)}${p.name} : ${mapType(repository, p.type)}" }.joinToString(", ")}) : ${returnType}")
            }
            if (setter) {
                appendln("    fun set(${it.parameters.map { p -> "${vararg(p)}${p.name} : ${mapType(repository, p.type)}" }.joinToString(", ")}) : ${returnType}")
            }
            if (it.name != "") {
                appendln("    fun ${it.name}(${it.parameters.map { p -> "${vararg(p)}${p.name} : ${mapType(repository, p.type)}" }.joinToString(", ")}) : ${returnType}")
            }
        }
    }
//
//    if ("get" !in interface.operations.map {it.name}) {
//        appendln()
//        interface.operations.filter { it.name !in superTypeOperations && it.name in listOf("item", "namedItem") && it.parameters.size() == 1 && mapType(repository, it.returnType) != "Unit" }.forEach {
//            appendln("    native(\"${it.name}\")")
//            appendln("    fun get(${it.parameters.map { p -> "${if (p.name.endsWith("...")) "vararg" else ""}${p.name} : ${mapType(repository, p.type)}" }.joinToString(", ")}) : ${mapType(repository, it.returnType)}")
//        }
//    }

    if (interface.constants.isNotEmpty()) {
        appendln("    companion object {")

        interface.constants.forEach {
            appendln("        val ${it.name} : ${mapType(repository, it.type)} = ${it.value}")
        }

        appendln("    }")
    }

    appendln()

    repository.externals[interface.name]
            ?.filter { "NoInterfaceObject" in (repository.interfaces[it]?.extendedAttributes?.map { it.call } ?: emptyList()) }
            ?.map { repository.interfaces[it]!! }
            ?.forEach { extensionInterface ->
                generateInterfaceBody(repository, extensionInterface)
            }
}
