// !DIAGNOSTICS: -UNUSED_VARIABLE

inline fun<T> foo(block: () -> T):T = block()

fun init(): String? { return null }

fun baz() {
    val x: String = foo {
        val task: String? = init()
        if (task == null) {
            return
        } else <!DEBUG_INFO_SMARTCAST!>task<!>
    }
}