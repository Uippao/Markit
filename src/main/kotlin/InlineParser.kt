package me.uippao.markit

object InlineParser {
    private fun parseLinkContent(content: String): Pair<String, String?> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return "" to null

        // Find the first whitespace that's not inside quotes
        var urlEnd = trimmed.length
        var inQuote: Char? = null

        for (i in trimmed.indices) {
            when (trimmed[i]) {
                ' ', '\t' -> if (inQuote == null) {
                    urlEnd = i
                    break
                }
                '"', '\'' -> {
                    if (inQuote == null) {
                        inQuote = trimmed[i]
                    } else if (inQuote == trimmed[i]) {
                        inQuote = null
                    }
                }
            }
        }

        val url = trimmed.substring(0, urlEnd).trim()
        val remaining = trimmed.substring(urlEnd).trim()

        if (remaining.isEmpty()) return url to null

        // Extract title if present (enclosed in quotes)
        val quote = remaining.firstOrNull()
        return if (quote == '\'' || quote == '"') {
            val endQuote = remaining.indexOf(quote, 1)
            if (endQuote != -1) {
                url to remaining.substring(1, endQuote)
            } else {
                url to null
            }
        } else {
            url to null
        }
    }
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

        return try {
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
                    input.startsWith("![", i) -> {
                        val closeBracket = input.indexOf("]", i+2)
                        val openParen = input.indexOf("(", closeBracket+1)
                        val closeParen = input.indexOf(")", openParen+1)

                        if (closeBracket != -1 && openParen == closeBracket+1 && closeParen != -1) {
                            val altText = input.substring(i+2, closeBracket)
                            val content = input.substring(openParen+1, closeParen)
                            val (url, title) = parseLinkContent(content)
                            result.add(ImageNode(url, altText, title))
                            i = closeParen + 1
                        } else {
                            result.add(TextNode(input.substring(i, i+2)))
                            i += 2
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
                    input.startsWith("`", i) -> {
                        val end = input.indexOf("`", i+1)
                        if (end != -1) {
                            val code = input.substring(i+1, end)
                            result.add(InlineCodeNode(code))
                            i = end + 1
                        } else {
                            result.add(TextNode(input[i].toString()))
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
        } catch (e: Exception) {
            listOf(TextNode(input))
        }
    }
}
