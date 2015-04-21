fun ff(<!UNUSED_PARAMETER!>a<!>: String) = 1

fun init(): String? { return "" }

fun gg() {
    val a: String? = init()

    if (a != null) {
        ff(<!DEBUG_INFO_SMARTCAST!>a<!>)
    }
}
