annotation class Ann(vararg val i: Int)

Ann(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>)
Ann(i2)
Ann(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>)
Ann(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>, i2, <!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>)
Ann(*intArrayOf(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>))
Ann(*intArrayOf(i2))
Ann(*intArrayOf(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>))
Ann(*intArrayOf(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>, i2, <!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>))
class Test

var i = 1
val i2 = 1
val i3 = foo()

fun foo(): Int = 1

annotation class AnnJC(vararg val i: Class<*>)
AnnJC(*arrayOf(javaClass<Test>()))
AnnJC(*arrayOf(<!ANNOTATION_PARAMETER_MUST_BE_CLASS_LITERAL!>iJC<!>))
class TestJC
val iJC = javaClass<Test>()

annotation class AnnAnn(vararg val i: Ann)
AnnAnn(*arrayOf(Ann(1)))
AnnAnn(*arrayOf(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>iAnn<!>))
class TestAnn
val iAnn = <!ANNOTATION_CLASS_CONSTRUCTOR_CALL!>Ann(1)<!>