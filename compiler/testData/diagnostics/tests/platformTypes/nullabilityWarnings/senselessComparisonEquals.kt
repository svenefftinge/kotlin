// !DIAGNOSTICS: -UNUSED_EXPRESSION

// FILE: p/J.java
package p;

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
    @Nullable
    public static J staticN;
    public static J staticJ;
}

// FILE: k.kt

import p.*

fun test() {
    // @NotNull platform type
    val platformNN = J.staticNN
    // @Nullable platform type
    val platformN = J.staticN
    // platform type with no annotation
    val platformJ = J.staticJ

    val a: Any? = null

    if (<!SENSELESS_COMPARISON!>platformNN != null<!>) {}
    if (<!SENSELESS_COMPARISON!>null != platformNN<!>) {}
    if (<!SENSELESS_COMPARISON!>platformNN == null<!>) {}
    if (<!SENSELESS_COMPARISON!>null == platformNN<!>) {}

    if (<!SENSELESS_COMPARISON!>a != null<!> && platformNN != a) {}

    if (platformN != null) {}
    if (platformN == null) {}
    if (<!SENSELESS_COMPARISON!>a == null<!> && platformN == a) {}

    if (platformJ != null) {}
    if (platformJ == null) {}
    if (<!SENSELESS_COMPARISON!>a == null<!> && platformN == a) {}
}

