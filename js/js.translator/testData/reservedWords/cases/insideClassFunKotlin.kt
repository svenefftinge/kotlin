package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

class TestClass {
    fun Kotlin() { Kotlin() }

    fun test() {
        testNotRenamed("Kotlin", { ::Kotlin })
    }
}

fun box(): String {
    TestClass().test()

    return "OK"
}