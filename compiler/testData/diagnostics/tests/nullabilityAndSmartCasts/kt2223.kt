//KT-2223 Comparing non-null value with null might produce helpful warning
package kt2223

fun foo() {
    val x: Int? = null
    if (<!SENSELESS_COMPARISON!>x == null<!>) return
    if (<!SENSELESS_COMPARISON!>x == null<!>) return
}
