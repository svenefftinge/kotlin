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

import org.antlr.webidl.WebIDLBaseListener
import org.antlr.webidl.WebIDLLexer
import org.antlr.webidl.WebIDLParser
import org.antlr.webidl.WebIDLParser.*
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.webidl.WebIDLBaseVisitor
import org.jsoup.Jsoup
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.URL
import java.util.*

data class ExtendedAttribute(val name : String?, val call : String, val arguments : List<String>)
data class Operation(val name : String, val returnType : String, val parameters : List<Attribute>, val attributes : List<ExtendedAttribute>)
data class Attribute(val name : String, val type : String, val readOnly : Boolean)
data class Constant(val name : String, val type : String, val value : String?)

enum class DefinitionType {
    INTERFACE
    TYPEDEF
    EXTENSION_INTERFACE
    ENUM
    DICTIONARY
}
trait Definition
data class TypedefDefinition(val from : String, val to : String) : Definition
data class InterfaceDefinition(val name : String, val extendedAttributes: List<ExtendedAttribute>, val operations : List<Operation>, val attributes : List<Attribute>, val superTypes : List<String>, val constants : List<Constant>, val dictionary : Boolean = false) : Definition
data class ExtensionInterfaceDefinition(val name : String, val implements : String) : Definition
data class EnumDefinition(val name : String) : Definition

class ExtendedAttributeParser : WebIDLBaseVisitor<ExtendedAttribute>() {
    private var name : String? = null
    private var call : String = ""
    private val arguments = ArrayList<String>()

    override fun defaultResult(): ExtendedAttribute = ExtendedAttribute(name, call, arguments)

    override fun visitOther(ctx: WebIDLParser.OtherContext): ExtendedAttribute {
        visitChildren(ctx)

        val text = ctx.getText()
        if (name == null && call == "") {
            call = text
        } else if (name == null) {
            name = call
            call = text
        } else {
            arguments.add(text)
        }

        return defaultResult()
    }

    override fun visitExtendedAttributeInner(ctx: WebIDLParser.ExtendedAttributeInnerContext): ExtendedAttribute {
        visitChildren(ctx)
        return defaultResult()
    }
}

class UnionTypeVisitor : WebIDLBaseVisitor<List<String>>() {
    val list = ArrayList<String>()

    override fun defaultResult() = list

    override fun visitUnionMemberType(ctx: WebIDLParser.UnionMemberTypeContext): List<String> {
        list.add(TypeVisitor().visitChildren(ctx))

        return list
    }
}

class TypeVisitor : WebIDLBaseVisitor<String>() {
    private var type = ""

    override fun defaultResult() = type

    override fun visitNonAnyType(ctx: WebIDLParser.NonAnyTypeContext): String {
        type = ctx.getText()
        return type
    }

    override fun visitUnionType(ctx: WebIDLParser.UnionTypeContext): String {
        type = "Union<" + UnionTypeVisitor().visitChildren(ctx).joinToString(",") + ">"
        return type
    }
}

class OperationVisitor(val attributes : List<ExtendedAttribute>) : WebIDLBaseVisitor<Operation>() {
    private var name : String = ""
    private var returnType : String = ""
    private val parameters = ArrayList<Attribute>()
    private val exts = ArrayList<ExtendedAttribute>()

    override fun defaultResult() = Operation(name, returnType, parameters, attributes + exts)

    override fun visitOptionalIdentifier(ctx: OptionalIdentifierContext): Operation {
        name = ctx.getText()
        return defaultResult()
    }

    override fun visitSpecial(ctx: WebIDLParser.SpecialContext): Operation {
        if (ctx.children != null) {
            exts.add(ExtendedAttribute(call = ctx.getText(), name = null, arguments = emptyList()))
        }

        return defaultResult()
    }

    override fun visitReturnType(ctx: ReturnTypeContext): Operation {
        returnType = TypeVisitor().visit(ctx)
        return defaultResult()
    }

    override fun visitOptionalOrRequiredArgument(ctx: WebIDLParser.OptionalOrRequiredArgumentContext): Operation {
        val attributeVisitor = AttributeVisitor(false, emptyList())
        attributeVisitor.visit(ctx)
        val parameter = attributeVisitor.visitChildren(ctx)

        parameters.add(parameter)

        return defaultResult()
    }
}

class AttributeVisitor(val readOnly : Boolean, val attributes : List<ExtendedAttribute>) : WebIDLBaseVisitor<Attribute>() {
    var type : String = ""
    var name : String = ""

    override fun defaultResult() : Attribute = Attribute(name, type, readOnly)

    override fun visitType(ctx: WebIDLParser.TypeContext): Attribute {
        type = TypeVisitor().visit(ctx)
        return defaultResult()
    }

    override fun visitAttributeRest(ctx: WebIDLParser.AttributeRestContext): Attribute {
        try {
            name = getName(ctx)
        } catch (ignore : Throwable) {
            name = ctx.children.filter { it is TerminalNode}.filter {it.getText() != ";"}.last().getText()
        }
        return defaultResult()
    }

    override fun visitArgumentName(ctx: WebIDLParser.ArgumentNameContext): Attribute {
        try {
            name = getName(ctx)
        } catch (ignore : Throwable) {
            name = ctx.getText()
        }
        return defaultResult()
    }
}

class ConstantVisitor(val attributes : List<ExtendedAttribute>) : WebIDLBaseVisitor<Constant>() {
    var type : String = ""
    var name : String = ""
    var value : String? = null

    override fun defaultResult() : Constant = Constant(name, type, value)

    override fun visitConst_(ctx: WebIDLParser.Const_Context): Constant {
        name = getName(ctx)

        return visitChildren(ctx)
    }

    override fun visitConstType(ctx: WebIDLParser.ConstTypeContext): Constant {
        type = ctx.getText()
        return defaultResult()
    }

    override fun visitConstValue(ctx: WebIDLParser.ConstValueContext): Constant {
        value = ctx.getText()
        return defaultResult()
    }
}

class DefinitionVisitor(val extendedAttributes: List<ExtendedAttribute>) : WebIDLBaseVisitor<Definition>() {
    private var type : DefinitionType = DefinitionType.INTERFACE
    private var name = ""
    private val memberAttributes = ArrayList<ExtendedAttribute>()
    private val operations = ArrayList<Operation>()
    private val attributes = ArrayList<Attribute>()
    private var readOnly : Boolean = false
    private val inherited = ArrayList<String>()
    private var singleType : String? = null
    private var implements : String? = null
    private val constants = ArrayList<Constant>()

    override fun defaultResult(): Definition = when(type) {
        DefinitionType.INTERFACE -> InterfaceDefinition(name, extendedAttributes, operations, attributes, inherited, constants)
        DefinitionType.DICTIONARY -> InterfaceDefinition(name, extendedAttributes, operations, attributes, inherited, constants, true)
        DefinitionType.EXTENSION_INTERFACE -> ExtensionInterfaceDefinition(name, implements ?: "")
        DefinitionType.TYPEDEF -> TypedefDefinition(singleType ?: "", name)
        DefinitionType.ENUM -> EnumDefinition(name)
    }

    override fun visitInterface_(ctx: Interface_Context) : Definition {
        name = getName(ctx)
        visitChildren(ctx)
        return defaultResult()
    }

    override fun visitPartialInterface(ctx: WebIDLParser.PartialInterfaceContext): Definition {
        name = getName(ctx)
        visitChildren(ctx)
        return defaultResult()
    }

    override fun visitTypedef(ctx: WebIDLParser.TypedefContext): Definition {
        type = DefinitionType.TYPEDEF
        name = getName(ctx)
        visitChildren(ctx)

        return defaultResult()
    }

    override fun visitEnum_(ctx: Enum_Context): Definition {
        type = DefinitionType.ENUM
        name = getName(ctx)

        return defaultResult()
    }

    override fun visitDictionary(ctx: DictionaryContext): Definition {
        type = DefinitionType.DICTIONARY
        name = getName(ctx)

        return visitChildren(ctx)
    }

    override fun visitDictionaryMember(ctx: DictionaryMemberContext): Definition {
        val name = ctx.children
                ?.filter {it is TerminalNode && it.getSymbol().getType() == WebIDLLexer.IDENTIFIER_WEBIDL}
                ?.first { it.getText() != "" }
                ?.getText()

        val type = TypeVisitor().visit(ctx)
        val defaultValue = object : WebIDLBaseVisitor<String?>() {
            override fun visitDefaultValue(ctx2: DefaultValueContext) : String? {
                return ctx2.getText()
            }
        }.visit(ctx)

        attributes.add(Attribute(name ?: "", type, false))

        return defaultResult()
    }

    override fun visitImplementsStatement(ctx: ImplementsStatementContext): Definition {
        val identifiers = ctx.children.filter {it is TerminalNode && it.getSymbol().getType() == WebIDLLexer.IDENTIFIER_WEBIDL}.map {it.getText()}

        if (identifiers.size() >= 2) {
            type = DefinitionType.EXTENSION_INTERFACE
            name = identifiers[0]
            implements = identifiers[1]
            visitChildren(ctx)
        }

        return defaultResult()
    }

    override fun visitSingleType(ctx: SingleTypeContext): Definition {
        singleType = ctx.getText()
        return defaultResult()
    }

    override fun visitOperation(ctx: OperationContext) : Definition {
        with(OperationVisitor(memberAttributes.toList())) {
            operations.add(visit(ctx))
        }
        memberAttributes.clear()
        return defaultResult()
    }

    override fun visitInheritance(ctx: WebIDLParser.InheritanceContext): Definition {
        if (ctx.children != null && ctx.isEmpty().not()) {
            inherited.add(getName(ctx))
        }
        return defaultResult()
    }

    override fun visitReadOnly(ctx: WebIDLParser.ReadOnlyContext): Definition {
        readOnly = true
        visitChildren(ctx)
        readOnly = false

        return defaultResult()
    }

    override fun visitAttributeRest(ctx: WebIDLParser.AttributeRestContext): Definition {
        with(AttributeVisitor(readOnly, memberAttributes)) {
            visit(ctx)
            this@DefinitionVisitor.attributes.add(visitChildren(ctx))
        }

        return defaultResult()
    }

    override fun visitConst_(ctx: WebIDLParser.Const_Context): Definition {
        constants.add(ConstantVisitor(memberAttributes.toList()).visit(ctx))
        memberAttributes.clear()

        return defaultResult()
    }

    override fun visitExtendedAttribute(ctx: ExtendedAttributeContext) : Definition {
        val att = with(ExtendedAttributeParser()) {
            visitChildren(ctx)
        }

        memberAttributes.add(att)
        return defaultResult()
    }

}

private fun getName(ctx: ParserRuleContext) = ctx.children.first {it is TerminalNode && it.getSymbol().getType() == WebIDLLexer.IDENTIFIER_WEBIDL}.getText()

fun parseIDL(reader : Reader) : Repository {
    val ll = WebIDLLexer(ANTLRInputStream(reader))
    val pp = WebIDLParser(CommonTokenStream(ll))

    val idl = pp.webIDL()

    val declarations = ArrayList<Definition>()

    idl.accept(object : WebIDLBaseVisitor<Unit>() {
        val extendedAttributes = ArrayList<ExtendedAttribute>()

        override fun visitDefinition(ctx: WebIDLParser.DefinitionContext) {
            val declaration = DefinitionVisitor(extendedAttributes).visitChildren(ctx)
            extendedAttributes.clear()
            declarations.add(declaration)
        }

        override fun visitExtendedAttribute(ctx: ExtendedAttributeContext?) {
            val att = with(ExtendedAttributeParser()) {
                visitChildren(ctx)
            }

            extendedAttributes.add(att)
        }
    })

    return Repository(
            declarations.filterIsInstance<InterfaceDefinition>().filter {it.name.isEmpty().not()}.groupBy { it.name }.mapValues { it.getValue().reduce(::merge) },
            declarations.filterIsInstance<TypedefDefinition>().groupBy { it.to }.mapValues { it.getValue().single() },
            declarations.filterIsInstance<ExtensionInterfaceDefinition>().groupBy { it.name }.mapValues { it.getValue().map {it.implements} },
            declarations.filterIsInstance<EnumDefinition>().groupBy { it.name }.mapValues { it.getValue().reduce {a, b -> a} }
    )
}

fun merge(i1 : InterfaceDefinition, i2 : InterfaceDefinition) : InterfaceDefinition {
    require(i1.name == i2.name)

    return InterfaceDefinition(i1.name,
            extendedAttributes = i1.extendedAttributes merge i2.extendedAttributes,
            operations = i1.operations merge i2.operations,
            attributes = i1.attributes merge i2.attributes,
            superTypes = i1.superTypes merge i2.superTypes,
            constants = i1.constants merge i2.constants
            )
}

fun <T> List<T>.merge(other : List<T>) = (this + other).distinct().toList()
