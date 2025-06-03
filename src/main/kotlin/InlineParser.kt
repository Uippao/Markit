package me.uippao.markit

object InlineParser {
    /**
     * Parses inline syntax in a string and returns a list of InlineNode.
     * Supports:
     * - **bold** with `**bold**`
     * - *italic* with `*italic*`
     * - [link](url)
     * - plain text
     */
    fun parseInline(input: String): List<InlineNode> {
        val result = mutableListOf<InlineNode>()
        var i = 0
        val n = input.length

        while (i < n) {
            when {
                input.startsWith("**", i) -> {
                    val end = input.indexOf("**", i + 2)
                    if (end != -1) {
                        val content = parseInline(input.substring(i + 2, end))
                        result.add(BoldNode(content))
                        i = end + 2
                    } else {
                        // no closing **, treat as text
                        result.add(TextNode(input.substring(i)))
                        break
                    }
                }
                input.startsWith("*", i) -> {
                    val end = input.indexOf("*", i + 1)
                    if (end != -1) {
                        val content = parseInline(input.substring(i + 1, end))
                        result.add(ItalicNode(content))
                        i = end + 1
                    } else {
                        result.add(TextNode(input.substring(i)))
                        break
                    }
                }
                input.startsWith("[", i) -> {
                    val closeBracket = input.indexOf("]", i + 1)
                    val openParen = input.indexOf("(", closeBracket + 1)
                    val closeParen = input.indexOf(")", openParen + 1)
                    if (closeBracket != -1 && openParen == closeBracket + 1 && closeParen != -1) {
                        val linkText = input.substring(i + 1, closeBracket)
                        val url = input.substring(openParen + 1, closeParen)
                        val content = parseInline(linkText)
                        result.add(LinkNode(url, content))
                        i = closeParen + 1
                    } else {
                        // malformed link, treat as text
                        result.add(TextNode(input.substring(i, i + 1)))
                        i++
                    }
                }
                else -> {
                    // consume plain text until special char or end
                    val nextSpecial = listOf(
                        input.indexOf("**", i),
                        input.indexOf("*", i),
                        input.indexOf("[", i)
                    ).filter { it != -1 }.minOrNull() ?: n
                    val text = input.substring(i, nextSpecial)
                    result.add(TextNode(text))
                    i = nextSpecial
                }
            }
        }

        return result
    }
}
