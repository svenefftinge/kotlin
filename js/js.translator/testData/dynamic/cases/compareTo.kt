package foo

fun init(arg: Any): dynamic { return arg }

fun box(): String {
    val a: dynamic = init(12)
    var b: dynamic = init(33.4)
    var c: dynamic = init("text")
    val d: dynamic = init(true)

    testFalse { a > b }
    testTrue { b <= 34 }
    testTrue { c >= "text" }
    testFalse { c <= "abc" }
    testTrue { d >= 1 }
    testFalse { d <= 0 }
    testTrue { d && true }
    testTrue { false || d }
    testFalse { d && a < c }

    return "OK"
}
