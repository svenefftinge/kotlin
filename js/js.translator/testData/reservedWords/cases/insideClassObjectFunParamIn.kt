package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

class TestClass {
    companion object {
        fun foo(`in`: String) {
    assertEquals("123", `in`)
    testRenamed("in", { `in` })
}

        fun test() {
            foo("123")
        }
    }
}

fun box(): String {
    TestClass.test()

    return "OK"
}