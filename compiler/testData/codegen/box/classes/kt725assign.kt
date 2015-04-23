public fun box() : String {
    var i : Int?
    i = 10
    val j = i++

    return if(j==10 && 11 == i) "OK" else "fail"
}
