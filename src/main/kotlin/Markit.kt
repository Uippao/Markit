package me.uippao.Markit

import me.uippao.markit.Transpiler
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun transpileMarkit(input: String): String {
    return Transpiler().transpile(input)
}

suspend fun transpileMarkitAsync(input: String): String = withContext(Dispatchers.Default) {
    Transpiler().transpile(input)
}

fun safeTranspileMarkit(input: String): String? {
    return try {
        Transpiler().transpile(input)
    } catch (e: Exception) {
        null
    }
}

fun transpileMarkitFile(filePath: String): String {
    val input = File(filePath).readText()
    return Transpiler().transpile(input)
}

fun transpileMarkitToFile(input: String, outputFilePath: String) {
    val output = Transpiler().transpile(input)
    File(outputFilePath).writeText(output)
}

fun transpileMarkitFileToFile(inputFilePath: String, outputFilePath: String) {
    val input = File(inputFilePath).readText()
    val output = Transpiler().transpile(input)
    File(outputFilePath).writeText(output)
}
