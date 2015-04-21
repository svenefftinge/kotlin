fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()

    do {
        bar(<!TYPE_MISMATCH!>x<!>)
    } while (x == null)
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    
    val y: Int? = init()
    do {
        bar(<!TYPE_MISMATCH!>y<!>)
    } while (y != null)
    bar(<!TYPE_MISMATCH!>y<!>)
}
