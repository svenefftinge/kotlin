fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    if (x != null) {
        when (x) {
            0 -> bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
            else -> {}
        }
    }

    when (x) {
        0 -> { if (<!SENSELESS_COMPARISON!>x == null<!>) return }
        else -> { if (x == null) return }
    }
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
}
