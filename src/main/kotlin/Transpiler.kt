package me.uippao.markit

class Transpiler {
    fun transpile(input: String): String {
        return try {
            val (withoutVars, varMap) = VariableExtractor.extractVariables(input)
            val afterVarSub = VariableExtractor.substituteVariables(withoutVars, varMap)
            val blocks = BlockParser.parseBlocks(afterVarSub)
            return HtmlGenerator.generateHtml(blocks)
        } catch (e: Exception) {
            "<pre>${Utils.escapeHtml(input)}</pre>"
        }
    }
}