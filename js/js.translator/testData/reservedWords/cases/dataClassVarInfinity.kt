package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

data class DataClass(var Infinity: String) {
    init {
        testNotRenamed("Infinity", { Infinity })
    }
}

fun box(): String {
    DataClass("123")

    return "OK"
}