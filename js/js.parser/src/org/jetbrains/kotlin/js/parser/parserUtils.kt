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

package org.jetbrains.kotlin.js.parser

import com.google.dart.compiler.backend.js.ast.JsFunction
import com.google.dart.compiler.backend.js.ast.JsFunctionScope
import com.google.dart.compiler.backend.js.ast.JsScope
import com.google.dart.compiler.backend.js.ast.JsStatement
import com.google.dart.compiler.common.SourceInfoImpl
import com.google.gwt.dev.js.JsAstMapper
import com.google.gwt.dev.js.rhino.*
import java.io.Reader
import java.io.StringReader
import java.util.Observable
import java.util.Observer

private val FAKE_SOURCE_INFO = SourceInfoImpl(null, 0, 0, 0, 0)

public fun parse(code: String, reporter: ErrorReporter, scope: JsScope): List<JsStatement> {
        val insideFunction = scope is JsFunctionScope
        val node = parse(code, 0, reporter, insideFunction, fun Parser.(stream: TokenStream) = parse(stream))
        return node.toJsAst(scope, fun JsAstMapper.(node: Node) = mapStatements(node))
}

public fun parseFunction(code: String, offset: Int, reporter: ErrorReporter, scope: JsScope): JsFunction =
        parse(code, offset, reporter, insideFunction = false) {
            addObserver(FunctionParsingObserver())
            primaryExpr(it)
        }.toJsAst(scope, fun JsAstMapper.(node: Node) = mapFunction(node))

private class FunctionParsingObserver : Observer {
    var functionsStarted = 0

    override fun update(o: Observable?, arg: Any?): Unit =
            when (arg) {
                is ParserEvents.OnFunctionParsingStart -> {
                    functionsStarted++
                }
                is ParserEvents.OnFunctionParsingEnd -> {
                    functionsStarted--

                    if (functionsStarted == 0) {
                        arg.tokenStream.ungetToken(TokenStream.EOF)
                    }
                }
            }
}

inline
private fun parse(
        code: String,
        offset: Int,
        reporter: ErrorReporter,
        insideFunction: Boolean,
        parseAction: Parser.(TokenStream)->Any
): Node {
    Context.enter().setErrorReporter(reporter)

    try {
        val ts = TokenStream(StringReader(code, offset), "<parser>", FAKE_SOURCE_INFO.getLine())
        val parser = Parser(IRFactory(ts), insideFunction)
        return parser.parseAction(ts) as Node
    } finally {
        Context.exit()
    }
}

inline
private fun Node.toJsAst<T>(scope: JsScope, mapAction: JsAstMapper.(Node)->T): T =
        JsAstMapper(scope).mapAction(this)

private fun StringReader(string: String, offset: Int): Reader {
    val reader = StringReader(string)
    reader.skip(offset.toLong())
    return reader
}