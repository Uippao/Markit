package me.uippao.markit

/** Convenience extension to directly transpile a Markit string to HTML */
fun String.markitToHTML(): String {
    return Transpiler().transpile(this)
}

fun String.safeMarkitToHTML(): String? {
    return try {
        Transpiler().transpile(this)
    } catch (e: Exception) {
        null
    }
}