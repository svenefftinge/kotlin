public fun box() : Int {
    var i : Int? 
    i = 10
    // KT-7561: both i and i++ should be Int
    val j : Int = <!DEBUG_INFO_SMARTCAST!>i<!>++
    // k also should be Int
    val k = <!DEBUG_INFO_SMARTCAST!>i<!>++
    // and m also
    val m = ++<!DEBUG_INFO_SMARTCAST!>i<!>
    return j + k + m + <!DEBUG_INFO_SMARTCAST!>i<!>
}
