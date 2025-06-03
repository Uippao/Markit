package me.uippao.markit

object VariableExtractor {
    private val variableRegex = Regex("\\$\\{(\\w+)}")

    /** Extract variables definitions from the input.
     * Returns a pair: input without variable lines, and a map of variable name to value */
    fun extractVariables(input: String): Pair<String, Map<String, String>> {
        val variables = mutableMapOf<String, String>()
        val lines = input.lines()
        val filteredLines = lines.filterNot { line ->
            val match = Regex("^\\$(\\w+)\\s*=\\s*(.*)$").find(line)
            if (match != null) {
                val (name, value) = match.destructured
                variables[name] = value.trim()
                true
            } else false
        }
        return filteredLines.joinToString("\n") to variables
    }

    /** Substitute all occurrences of variables like ${varName} with their values */
    fun substituteVariables(input: String, vars: Map<String, String>): String {
        return variableRegex.replace(input) {
            val key = it.groupValues[1]
            vars[key] ?: it.value // Leave unchanged if not found
        }
    }
}
