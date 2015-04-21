fun bar(x: Int) = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    fun baz() = bar(<!TYPE_MISMATCH!>x<!>)
    fun quux() = if (x != null) bar(<!DEBUG_INFO_SMARTCAST!>x<!>) else baz()
    fun quuux() = bar(if (x == null) 0 else <!DEBUG_INFO_SMARTCAST!>x<!>)
}
