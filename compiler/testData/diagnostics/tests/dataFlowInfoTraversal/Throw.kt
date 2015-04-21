fun bar(x: Int): RuntimeException = RuntimeException(x.toString())

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    if (x == null) throw bar(<!TYPE_MISMATCH!>x<!>)
    throw bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    <!UNREACHABLE_CODE!>throw bar(<!DEBUG_INFO_SMARTCAST!>x<!>)<!>
}