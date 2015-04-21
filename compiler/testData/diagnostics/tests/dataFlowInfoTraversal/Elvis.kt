fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    bar(x ?: 0)
    if (x != null) bar(<!USELESS_ELVIS!>x<!> ?: <!DEBUG_INFO_SMARTCAST!>x<!>)
    bar(<!TYPE_MISMATCH!>x<!>)
}