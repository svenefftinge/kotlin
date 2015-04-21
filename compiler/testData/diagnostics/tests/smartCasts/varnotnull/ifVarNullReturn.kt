public fun fooNotNull(s: String) {
    System.out.println("Length of $s is ${s.length()}")
}

fun init(): String? { return "not null" }

public fun foo() {
    var s: String? = init()
    if (s == null) {
        return
    }
    fooNotNull(<!DEBUG_INFO_SMARTCAST!>s<!>)
}
