//KT-597 Type inference failed

fun <T> Array<T>?.get(i: Int) : T {
    if (this != null)
        return <!DEBUG_INFO_SMARTCAST!>this<!>.get(i) // <- inferred type is Any? but &T was excepted
    else throw NullPointerException()
}

fun Int?.inc() : Int {
    if (this != null)
        return <!DEBUG_INFO_SMARTCAST!>this<!>.inc()
    else
        throw NullPointerException()
}

fun test() {
   var i : Int? = 10
   var <!UNUSED_VARIABLE!>i_inc<!> = <!UNUSED_CHANGED_VALUE!><!DEBUG_INFO_SMARTCAST!>i<!>++<!> // <- expected Int?, but returns Any?
}
