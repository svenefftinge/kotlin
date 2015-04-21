fun foo() {
    var v: Any = "xyz"
    // Smart cast is possible here: initialization
    <!DEBUG_INFO_SMARTCAST!>v<!>.length()
    v = 42
    v.<!UNRESOLVED_REFERENCE!>length<!>()
}