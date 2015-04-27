trait Z {
    fun doIt()
}

open class A: Z {
    override fun doIt() {}
}

class E: Z {
    override fun doIt() {}
}

abstract class Wrapper<T: A>(protected val t: T) {
    fun doIt() { t.doIt(); t.toString() }
}

class MyWrapper(a: A): Wrapper<A>(a)

// This wrapper is not legal
class TheirWrapper(e: E): Wrapper<<!UPPER_BOUND_VIOLATED, UPPER_BOUND_VIOLATED!>E<!>>(e)

data class Pair<out T>(val a: T, val b: T) {
    fun doIt() { a.toString(); b.toString() }
}

fun <T> pairOf(vararg values: T): Pair<T> { return Pair(values[0], values[1]) }

fun foo(): String {
    val matrix: Pair<Wrapper<*>>
    // It's not legal to do such a thing because E is not derived from A
    // But we should not have assertion errors because of it!
    matrix = pairOf(MyWrapper(A()), TheirWrapper(E()))
    matrix.doIt()
    return matrix.toString()
}
