// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    bar(1 + (if (x == null) 0 else x))
    bar(if (x == null) <!TYPE_MISMATCH!>x<!> else x)
    if (x != null) bar(x + x/(x-x*x))
}
