fun init(): Boolean? { return null }

fun main(args: Array<String>) {
    val b: Boolean? = init()
    if (b != null) {
        if (!<!DEBUG_INFO_SMARTCAST!>b<!>) {} // OK
        if (<!DEBUG_INFO_SMARTCAST!>b<!>) {} // Error: Condition must be of type kotlin.Boolean, but is of type kotlin.Boolean?
        if (b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {} // WARN: Unnecessary non-null assertion (!!) on a non-null receiver of type kotlin.Boolean?
        foo(<!DEBUG_INFO_SMARTCAST!>b<!>) // OK
    }
}

fun foo(a: Boolean) = a
