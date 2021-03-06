fun <caret>foo(s: String, x: X, k: Int): Boolean {
    return x.k + s.length() - k > 0
}

class X(val k: Int)

fun <T, R> with(receiver: T, f: T.() -> R): R = receiver.f()

fun test() {
    foo("1", X(0), 2)
    with(X(0)) {
        foo("1", this, 2)
    }
}