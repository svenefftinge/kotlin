class A() {
  override fun equals(<warning>a</warning> : Any?) : Boolean = false
}

fun f(): Unit {
  var x: Int? = <warning>1</warning>
  x = 1
  x + 1
  x plus 1
  x < 1
  x += 1

  x == 1
  x != 1

  <error>A() == 1</error>

  <error>x === "1"</error>
  <error>x !== "1"</error>

  x === 1
  x !== 1

  x..2
  x in 1..2

  val y : Boolean? = true
  <warning>false || y</warning>
  <warning>y && true</warning>
  <warning>y && <error>1</error></warning>
}