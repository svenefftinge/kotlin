//KT-2212 Incomplete nullability information
package kt2212

fun init(): Int? { return 1 }

fun main(args: Array<String>) {
    val x: Int? = init()
    if (x == null) return
    System.out.println(<!DEBUG_INFO_SMARTCAST!>x<!>.plus(x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))
}
