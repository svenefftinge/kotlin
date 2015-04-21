class Bar {
    fun next(): Bar? {
        if (2 == 4)
            return this
        else
            return null
    }
}

fun foo(): Bar {
    var x: Bar? = Bar()
    var y: Bar? = Bar()
    // NB: both x and y must be reset here
    while (x != null) {
        // Here call is unsafe because of the inner loop (BUG)
        y<!UNSAFE_CALL!>.<!>next()
        // NB: y must be reset here
        while (y != null) {
            if (x == y)
                // x is not null because of outer while
                return <!DEBUG_INFO_SMARTCAST!>x<!>
            // y is not null because of inner while
            y = <!DEBUG_INFO_SMARTCAST!>y<!>.next()
        }
        // x is not null because of outer while
        x = <!DEBUG_INFO_SMARTCAST!>x<!>.next()
    }
    return Bar()
}