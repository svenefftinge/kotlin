package org.jetbrains.idl2k

import java.io.File
import java.io.StringReader

fun main(args: Array<String>) {
    val case = 2
    when (case) {
        1 -> main1(args)
        2 -> main2(args)
    }
}

fun main1(args: Array<String>) {
    val defs = parseIDL(StringReader("""
dictionary EventInit {
  boolean bubbles = false;
  boolean cancelable = false;
};
    """))

    defs.interfaces.values().filter { "NoInterfaceObject" !in it.extendedAttributes.map { it.call } }.forEach {
        System.out.generateInterface(defs, it)
    }
}

fun main2(args: Array<String>) {
    val idl = getAllIDLs()
    val defs = parseIDL(StringReader(idl))

    println("IDL dump:")
    idl.lineSequence().forEachIndexed { i, line ->
        println("${i.toString().padStart(4, ' ')}: ${line}")
    }
    println()

    File("/home/cy/IdeaProjects/bug/src/m.kt").writer().use { w ->
        //    File("src/main/kotlin/tmp.kt").writer().use { w ->
        w.appendln("package org.w3c.dom2")
        w.appendln()

        defs.interfaces.values().filter { "NoInterfaceObject" !in it.extendedAttributes.map { it.call } }.forEach {
            w.generateInterface(defs, it)
        }
    }
}