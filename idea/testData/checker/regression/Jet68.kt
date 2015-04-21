class Foo()

fun test() {
  val f : Foo? = null
  if (<warning>f == null</warning>) {

  }
  if (<warning>f != null</warning>) {

  }
}