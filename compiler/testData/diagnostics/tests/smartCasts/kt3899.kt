data class StringPair(val first: String, val second: String)

fun String.to(second: String) = StringPair(this, second)

fun hashMapOf(<!UNUSED_PARAMETER!>pair<!>: StringPair): MutableMap<String, String> {
    <!UNREACHABLE_CODE!>return<!> null!!
}

fun init(): String? { return "xyz" }

fun F() : MutableMap<String, String> {
    val value: String? = init()
    if (value == null) throw Error()
    // Smart cast should be here
    return hashMapOf("sss" to <!DEBUG_INFO_SMARTCAST!>value<!>)  
}