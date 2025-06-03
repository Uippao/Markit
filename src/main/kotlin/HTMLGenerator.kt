package me.uippao.markit

object HtmlGenerator {

    /** Main entry to generate HTML from a list of BlockNode */
    fun generateHtml(blocks: List<BlockNode>): String {
        return blocks.joinToString("\n") { blockToHtml(it) }
    }

    private fun blockToHtml(node: BlockNode): String = when (node) {
        is ParagraphNode -> "<p>${inlineToHtml(node.content)}</p>"
        is HeadingNode -> "<h${node.level}>${inlineToHtml(node.content)}</h${node.level}>"
        is CodeBlockNode -> {
            val langClass = node.language?.let { " class=\"language-$it\"" } ?: ""
            val titleBar = node.language?.let { languageTitleBar(it) } ?: ""
            val copyButton = copyButtonHtml()
            """
            <div class="code-block">
              $titleBar
              <pre><code$langClass>${Utils.escapeHtml(node.content)}</code></pre>
              $copyButton
            </div>
            """.trimIndent()
        }
        is CollapseBlockNode -> {
            val contentHtml = generateHtml(node.content)
            """
            <details>
              <summary>${Utils.escapeHtml(node.summary)}</summary>
              $contentHtml
            </details>
            """.trimIndent()
        }
        is AlignBlockNode -> {
            val contentHtml = generateHtml(node.content)
            "<div style=\"text-align:${Utils.escapeHtml(node.alignment)};\">\n$contentHtml\n</div>"
        }
        is HtmlBlockNode -> node.content // raw HTML, trusted
        is TableBlockNode -> {
            val headersHtml = node.headers.joinToString("") { "<th>${Utils.escapeHtml(it)}</th>" }
            val rowsHtml = node.rows.joinToString("\n") { row ->
                "<tr>" + row.joinToString("") { "<td>${Utils.escapeHtml(it)}</td>" } + "</tr>"
            }
            val copyButton = copyButtonHtml()
            """
            <div class="table-container">
            <table>
              <thead><tr>$headersHtml</tr></thead>
              <tbody>
              $rowsHtml
              </tbody>
            </table>
            $copyButton
            </div>
            """.trimIndent()
        }
        is BlockQuoteNode -> {
            val contentHtml = generateHtml(node.content)
            "<blockquote>\n$contentHtml\n</blockquote>"
        }
        is ListBlockNode -> {
            renderList(node.items)
        }
        is DimmedNoteNode -> "<div class=\"dimmed-note\">${inlineToHtml(node.content)}</div>"
        is ButtonNode -> {
            val attrString = node.attributes.entries.joinToString(" ") { (k, v) -> "$k=\"$v\"" }
            "<button $attrString>${Utils.escapeHtml(node.label)}</button>"
        }
    }

    private fun inlineToHtml(inlineNodes: List<InlineNode>): String =
        inlineNodes.joinToString("") { inlineNodeToHtml(it) }

    private fun inlineNodeToHtml(node: InlineNode): String = when (node) {
        is TextNode -> Utils.escapeHtml(node.text)
        is BoldNode -> "<strong>${inlineToHtml(node.content)}</strong>"
        is ItalicNode -> "<em>${inlineToHtml(node.content)}</em>"
        is LinkNode -> "<a href=\"${Utils.escapeHtml(node.url)}\">${inlineToHtml(node.content)}</a>"
    }

    private fun renderList(items: List<ListItem>): String {
        // We render nested lists based on item.depth.
        // Items with increasing depth open <ul>, decreasing close.
        val sb = StringBuilder()
        var currentDepth = 0

        fun openList() = sb.append("<ul>\n")
        fun closeList() = sb.append("</ul>\n")

        for (item in items) {
            while (currentDepth < item.depth) {
                openList()
                currentDepth++
            }
            while (currentDepth > item.depth) {
                closeList()
                currentDepth--
            }
            sb.append("<li>\n")
            sb.append(generateHtml(item.content))
            sb.append("</li>\n")
        }
        while (currentDepth > 0) {
            closeList()
            currentDepth--
        }
        return sb.toString()
    }

    private fun languageTitleBar(language: String): String {
        val langEsc = Utils.escapeHtml(language)
        return """
            <div class="code-title-bar">
                <span class="language-label">$langEsc</span>
                <button class="copy-button" onclick="copyCode(this)">Copy</button>
            </div>
        """.trimIndent()
    }

    private fun copyButtonHtml(): String = """
        <button class="copy-button" onclick="copyCode(this)">Copy</button>
    """.trimIndent()
}
