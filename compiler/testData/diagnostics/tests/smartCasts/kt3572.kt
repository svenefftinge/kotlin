trait Printer {
    fun print()
}

class OKPrinter : Printer {
    override fun print() {  }
}

class MyClass(var printer: Printer)


fun main(m: MyClass) {
    if (m.printer is OKPrinter) {
        m.printer.print()
    }
}