// IS_APPLICABLE: false
// ERROR: Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type kotlin.String?

fun init(): String? { return "foo" }

fun main(args: Array<String>) {
    val foo: String? = init()
    if<caret> {
        foo.length()
    } else null
}
