fun Int?.inc() = this!!.inc()

public fun box() : String {
    var i : Int? = 10
    val j = ++i

    return if(j==11 && 11 == i) "OK" else "fail"
}
