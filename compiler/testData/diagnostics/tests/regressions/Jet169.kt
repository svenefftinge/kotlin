fun init(): String? { return "" }

fun set(key : String, value : String) {
  val a : String? = init()
  when (a) {
    "" -> <!DEBUG_INFO_SMARTCAST!>a<!>.get(0)
    is String, is Any -> <!DEBUG_INFO_SMARTCAST!>a<!>.compareTo("")
    else -> a.toString()
  }
}
