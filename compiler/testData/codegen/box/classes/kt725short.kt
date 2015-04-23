fun Short?.inc() = this!!.inc()

public fun box() : String {
    var i : Short? = 10
    val j = i++

    return if (j!!.toInt()==10 && i!!.toInt()==11) "OK" else "fail"
}
