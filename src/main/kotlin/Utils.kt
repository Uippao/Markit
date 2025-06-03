package me.uippao.markit

object Utils {
    fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun indent(text: String, level: Int = 1, indentStr: String = "  "): String {
        val indentation = indentStr.repeat(level)
        return text.lines().joinToString("\n") { if (it.isBlank()) it else indentation + it }
    }
}
