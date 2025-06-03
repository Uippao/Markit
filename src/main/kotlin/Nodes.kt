package me.uippao.markit

// Base interface for all block nodes
sealed interface BlockNode

// Inline node for inline elements (text, bold, italic, links, etc.)
sealed interface InlineNode

// Paragraph block
data class ParagraphNode(val content: List<InlineNode>) : BlockNode

// Heading block
data class HeadingNode(val level: Int, val content: List<InlineNode>) : BlockNode

// Code block
data class CodeBlockNode(val language: String?, val content: String) : BlockNode

// Collapse block
data class CollapseBlockNode(val summary: String, val content: List<BlockNode>) : BlockNode

// Align block
data class AlignBlockNode(val alignment: String, val content: List<BlockNode>) : BlockNode

// HTML raw block
data class HtmlBlockNode(val content: String) : BlockNode

// Table block
data class TableBlockNode(val headers: List<String>, val rows: List<List<String>>) : BlockNode

// Blockquote
data class BlockQuoteNode(val content: List<BlockNode>) : BlockNode

// List block and list items
data class ListBlockNode(val items: List<ListItem>) : BlockNode

data class ListItem(val depth: Int, val content: List<BlockNode>)

// Dimmed note
data class DimmedNoteNode(val content: List<InlineNode>) : BlockNode

// Button block
data class ButtonNode(val label: String, val attributes: Map<String, String>) : BlockNode

// Inline nodes

data class TextNode(val text: String) : InlineNode

data class BoldNode(val content: List<InlineNode>) : InlineNode

data class ItalicNode(val content: List<InlineNode>) : InlineNode

data class LinkNode(val url: String, val content: List<InlineNode>) : InlineNode

data class ImageNode(val url: String, val altText: String, val title: String?) : InlineNode

data class InlineCodeNode(val code: String) : InlineNode

// You can add more inline nodes as needed (e.g., code spans, images, etc.)

// Exception for parsing errors
class ParseException(message: String) : Exception(message)
