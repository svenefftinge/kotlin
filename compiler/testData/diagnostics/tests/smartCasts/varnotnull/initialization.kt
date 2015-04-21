fun foo() {
    var v: String? = "xyz"
    // Smart cast here: initialization
    <!DEBUG_INFO_SMARTCAST!>v<!>.length()
    v = null
    v<!UNSAFE_CALL!>.<!>length()
}