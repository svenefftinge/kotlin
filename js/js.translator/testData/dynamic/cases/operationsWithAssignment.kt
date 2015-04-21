package foo

fun init(arg: Any): dynamic { return arg }

fun box(): String {
    var a: dynamic = init(12)
    var b: dynamic = init(33.4)
    var c: dynamic = init("text")
    val d: dynamic = true

    a += d
    assertEquals(13, a)
    c += a
    assertEquals("text13", c)
    a %= 7
    assertEquals(6, a)
    b -= 32
    assertEquals(1.3999999999999986, b)
    b *= n
    assertEquals(58.79999999999994, b)
    b /= a
    assertEquals(9.79999999999999, b)
    c += a * 3 + b / n
    assertEquals("text1318.233333333333334", c)

    return "OK"
}
