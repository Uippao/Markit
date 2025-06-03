package me.uippao.markit

object BlockParser {
    fun parseBlocks(input: String): List<BlockNode> {
        val blocks = mutableListOf<BlockNode>()
        val lines = input.lines().toMutableList()
        var i = 0

        while (i < lines.size) {
            try {
                if (lines[i].isBlank()) {
                    i++
                    continue
                }

                when {
                    // Handle multi-line blocks first
                    lines[i].startsWith("```") -> {
                        val (block, consumed) = parseCodeBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].startsWith("@collapse") -> {
                        val (block, consumed) = parseCollapseBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].startsWith("@align") -> {
                        val (block, consumed) = parseAlignBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].startsWith("@html") -> {
                        val (block, consumed) = parseHtmlBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].startsWith("|") && i + 1 < lines.size &&
                            lines[i + 1].matches(Regex(".*\\|.*-+.*")) -> {
                        val (block, consumed) = parseTableBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    // Handle single-line blocks
                    lines[i].startsWith("-#") -> {
                        blocks.add(parseDimmedNote(lines[i]))
                        i++
                    }
                    lines[i].startsWith("@button") -> {
                        blocks.add(parseButton(lines[i]))
                        i++
                    }
                    lines[i].startsWith(">") -> {
                        val (block, consumed) = parseBlockQuote(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].trim().startsWith("- ") -> {
                        val (block, consumed) = parseListBlock(lines, i)
                        blocks.add(block)
                        i += consumed
                    }
                    lines[i].startsWith("#") -> {
                        blocks.add(parseHeading(lines[i]))
                        i++
                    }
                    else -> {
                        blocks.add(parseParagraph(lines[i]))
                        i++
                    }
                }
            } catch (e: Exception) {
                blocks.add(ParagraphNode(listOf(TextNode(lines[i]))))
                i++
            }
        }

        return blocks
    }

    private fun parseCodeBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val language = lines[start].substring(3).trim().takeIf { it.isNotBlank() }
        val content = mutableListOf<String>()
        var i = start + 1

        while (i < lines.size && !lines[i].startsWith("```")) {
            content.add(lines[i])
            i++
        }

        if (i >= lines.size) throw ParseException("Unclosed code block at line $start")
        return CodeBlockNode(language, content.joinToString("\n")) to (i - start + 1)
    }

    private fun parseCollapseBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val match = Regex("@collapse\\[(.*)\\]").matchEntire(lines[start])
        val summary = match?.groupValues?.get(1) ?: "Details"
        val content = mutableListOf<String>()
        var i = start + 1

        while (i < lines.size && !lines[i].startsWith("@/collapse")) {
            content.add(lines[i])
            i++
        }

        if (i >= lines.size) throw ParseException("Unclosed collapse block at line $start")
        val innerBlocks = parseBlocks(content.joinToString("\n"))
        return CollapseBlockNode(summary, innerBlocks) to (i - start + 1)
    }

    private fun parseAlignBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val match = Regex("@align\\[(.*)\\]").matchEntire(lines[start])
        val alignment = match?.groupValues?.get(1) ?: "left"
        val content = mutableListOf<String>()
        var i = start + 1

        while (i < lines.size && !lines[i].startsWith("@/align")) {
            content.add(lines[i])
            i++
        }

        if (i >= lines.size) throw ParseException("Unclosed align block at line $start")
        val innerBlocks = parseBlocks(content.joinToString("\n"))
        return AlignBlockNode(alignment, innerBlocks) to (i - start + 1)
    }

    private fun parseHtmlBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val content = mutableListOf<String>()
        var i = start + 1

        while (i < lines.size && !lines[i].startsWith("@/html")) {
            content.add(lines[i])
            i++
        }

        if (i >= lines.size) throw ParseException("Unclosed HTML block at line $start")
        return HtmlBlockNode(content.joinToString("\n")) to (i - start + 1)
    }

    private fun parseTableBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val rows = mutableListOf<List<String>>()
        var i = start

        while (i < lines.size && lines[i].contains('|')) {
            val row = lines[i].split('|').map { it.trim() }.filter { it.isNotBlank() }
            if (row.isNotEmpty()) rows.add(row)
            i++
        }

        if (rows.size < 2) throw ParseException("Invalid table at line $start")
        return TableBlockNode(rows[0], rows.subList(2, rows.size)) to (i - start)
    }

    private fun parseBlockQuote(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val content = mutableListOf<String>()
        var i = start

        while (i < lines.size && lines[i].startsWith(">")) {
            content.add(lines[i].substring(1).trimStart())
            i++
        }

        val innerBlocks = parseBlocks(content.joinToString("\n"))
        return BlockQuoteNode(innerBlocks) to (i - start)
    }

    private fun parseListBlock(lines: List<String>, start: Int): Pair<BlockNode, Int> {
        val items = mutableListOf<ListItem>()
        var i = start
        var currentDepth = -1
        var currentItem: MutableList<BlockNode>? = null

        fun closeItem() {
            if (currentItem != null) {
                items.add(ListItem(currentDepth, currentItem!!))
                currentItem = null
            }
        }

        while (i < lines.size && (lines[i].startsWith(" ") || lines[i].startsWith("-"))) {
            val line = lines[i]
            val depth = line.takeWhile { it == ' ' }.length / 2
            val content = line.trimStart().substringAfter("- ").trim()

            if (depth > currentDepth) {
                closeItem()
                currentDepth = depth
                currentItem = mutableListOf(ParagraphNode(InlineParser.parseInline(content)))
            } else if (depth < currentDepth) {
                closeItem()
                currentDepth = depth
                currentItem = mutableListOf(ParagraphNode(InlineParser.parseInline(content)))
            } else {
                currentItem?.add(ParagraphNode(InlineParser.parseInline(content)))
            }

            i++
        }

        closeItem()
        return ListBlockNode(items) to (i - start)
    }

    private fun parseDimmedNote(line: String): BlockNode {
        val content = line.substringAfter("-#").trim()
        return DimmedNoteNode(InlineParser.parseInline(content))
    }

    private fun parseButton(line: String): BlockNode {
        val match = Regex("@button\\[(.+?)\\](?:\\{(.+?)\\})?").matchEntire(line)
        val label = match?.groupValues?.get(1) ?: "Button"
        val attributes = mutableMapOf<String, String>()

        match?.groupValues?.get(2)?.split(',')?.forEach { part ->
            val keyValue = part.split('=')
            if (keyValue.size == 2) {
                attributes[keyValue[0].trim()] = keyValue[1].trim().removeSurrounding("\"")
            }
        }

        return ButtonNode(label, attributes)
    }

    private fun parseHeading(line: String): BlockNode {
        val match = Regex("^(#+)\\s+(.*)").find(line)
        val level = match?.groupValues?.get(1)?.length ?: 1
        val text = match?.groupValues?.get(2) ?: line
        return HeadingNode(level, InlineParser.parseInline(text))
    }

    private fun parseParagraph(line: String): BlockNode {
        return ParagraphNode(InlineParser.parseInline(line))
    }
}
