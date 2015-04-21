fun bar(x: Int): Int = x + 1

fun init(): Int? { return null }

fun foo() {
    val x: Int? = init()
    while (x == null) {
        bar(<!TYPE_MISMATCH!>x<!>)
    }
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    
    val y: Int? = init()
    while (y != null) {
        bar(<!DEBUG_INFO_SMARTCAST!>y<!>)
    }
    bar(<!TYPE_MISMATCH!>y<!>)
    
    val z: Int? = init()
    while (z == null) {
        bar(<!TYPE_MISMATCH!>z<!>)
        break
    }
    bar(<!TYPE_MISMATCH!>z<!>)
}
