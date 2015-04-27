package org.jetbrains.idl2k

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URL

val urls = listOf(
        "https://raw.githubusercontent.com/whatwg/html-mirror/master/source",
        "https://raw.githubusercontent.com/whatwg/dom/master/dom.html",
        "https://raw.githubusercontent.com/whatwg/xhr/master/Overview.src.html",
        "https://raw.githubusercontent.com/w3c/FileAPI/gh-pages/index.html",
        "https://raw.githubusercontent.com/whatwg/notifications/master/notifications.html",
        "https://raw.githubusercontent.com/whatwg/fullscreen/master/Overview.src.html",
        "http://dev.w3.org/csswg/cssom/"
)

val constantPart = """
[NoInterfaceObject]
interface HasStyle {
    attribute style : dynamic
};

HTMLElement implements HasStyle;
"""

private fun extractIDLText(url: String, out: StringBuilder) {
//    val soup = Jsoup.connect(url).validateTLSCertificates(false).ignoreHttpErrors(true).get()
    val soup = Jsoup.parse(URL(url).readText())
    fun append(it : Element) {
        val text = it.text()
        out.appendln(text)
        if (!text.trimEnd().endsWith(";")) {
            out.appendln(";")
        }
    }

    soup.select("pre.idl").filter {!it.hasClass("extract")}.forEach(::append)
    soup.select("code.idl-code").forEach(::append)
}

fun getAllIDLs(): String =
    StringBuilder {
        urls.forEach {
            extractIDLText(it, this)
        }

//        appendln(constantPart)
    }.toString()

