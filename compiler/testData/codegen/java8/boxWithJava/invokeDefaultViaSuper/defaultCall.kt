class Child : Test<String> {

    override fun testDefault(p: String): String {
        return "O" + super.testDefault(p)
    }
}
fun box(): String {
    return Child().testDefault("K")
}
