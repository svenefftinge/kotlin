fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()
    
    bar(<!TYPE_MISMATCH!>x<!>)
    if (x == null) return
    try {
        bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    }
    catch (e: Exception) {
        bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    }
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
}
