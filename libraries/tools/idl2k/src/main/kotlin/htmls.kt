package org.jetbrains.idl2k

import org.jsoup.Jsoup
import java.net.URL

val urls = listOf(
        "https://raw.githubusercontent.com/whatwg/html-mirror/master/source",
        "https://raw.githubusercontent.com/whatwg/dom/master/dom.html",
        "https://raw.githubusercontent.com/whatwg/xhr/master/Overview.src.html",
        "https://raw.githubusercontent.com/w3c/FileAPI/gh-pages/index.html"
)

private fun extractIDLText(url: String, out: StringBuilder) {
//    val soup = Jsoup.connect(url).validateTLSCertificates(false).ignoreHttpErrors(true).get()
    val soup = Jsoup.parse(URL(url).readText())
    soup.select("pre.idl").filter {!it.hasClass("extract")}.forEach {
        out.appendln(it.text())
    }
    soup.select("code.idl-code").forEach {
        out.appendln(it.text())
    }
}

fun getAllIDLs(): String =
    StringBuilder {
        urls.forEach {
            extractIDLText(it, this)
        }
    }.toString()

