// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo(): Int {
    val x: Int? = init()

    bar(<!TYPE_MISMATCH!>x<!>)
    if (x != null) return x
    
    val y: Int? = init()
    if (y == null) return if (<!SENSELESS_COMPARISON!>y != null<!>) y else <!TYPE_MISMATCH!>y<!>
    
    val z: Int? = init()
    if (z != null) return if (<!SENSELESS_COMPARISON!>z == null<!>) z else z
    
    return <!TYPE_MISMATCH!>z<!>
}
