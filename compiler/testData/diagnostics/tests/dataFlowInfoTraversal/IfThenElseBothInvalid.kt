fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    bar(<!TYPE_MISMATCH!>x<!>)
    if (x != 2) {
        if (x == null) return
        2<!OVERLOAD_RESOLUTION_AMBIGUITY!>+<!><!SYNTAX!><!>
    }
    else {
        if (<!SENSELESS_COMPARISON!>x == null<!>) return
        2<!OVERLOAD_RESOLUTION_AMBIGUITY!>+<!><!SYNTAX!><!>
    }
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
}
