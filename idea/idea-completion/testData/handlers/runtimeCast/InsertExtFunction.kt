fun init(): Base { return Derived() }

fun main(args: Array<String>) {
    val b: Base = init()
    <caret>val a = 1
}

open class Base

class Derived: Base()

fun Derived.funInDerived() { }

// RUNTIME_TYPE: Derived