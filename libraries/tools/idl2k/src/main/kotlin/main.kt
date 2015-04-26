package org.jetbrains.idl2k

import java.io.File
import java.io.StringReader

fun main(args: Array<String>) {
//    val idl = getAllIDLs()
    val defs = parseIDL(StringReader("""
    [Constructor(a,b,c)]
    interface Z {};
    """))

//    println("IDL dump:")
//    idl.lineSequence().forEachIndexed { i, line ->
//        println("${i.toString().padStart(4, ' ')}: ${line}")
//    }
//    println()

//    File("/home/cy/IdeaProjects/bug/src/m.kt").writer().use { w ->
    System.out.use { w ->
        //    File("src/main/kotlin/tmp.kt").writer().use { w ->
        w.appendln("package org.w3c.dom2")
        w.appendln()

        defs.interfaces.values().filter { "NoInterfaceObject" !in it.extendedAttributes.map { it.call } }.forEach {
            w.generateInterface(defs, it)
        }
    }
}