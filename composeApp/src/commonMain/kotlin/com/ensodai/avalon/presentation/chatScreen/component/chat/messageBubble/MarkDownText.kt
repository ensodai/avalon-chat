//package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble
//
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.unit.sp
//import org.intellij.markdown.MarkdownElementTypes
//import org.intellij.markdown.MarkdownTokenTypes
//import org.intellij.markdown.ast.ASTNode
//import org.intellij.markdown.ast.getTextInNode
//import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
//import org.intellij.markdown.parser.MarkdownParser
//
//@Composable
//fun MarkdownText(
//    markdown: String,
//    modifier: Modifier = Modifier,
//    style: TextStyle = TextStyle.Default,
//    linkColor: Color = Color.Blue
//) {
//    val annotatedString = remember(markdown, style, linkColor) {
//        val flavour = CommonMarkFlavourDescriptor()
//        val parsedTree = MarkdownParser(flavour).parse(MarkdownElementTypes.MARKDOWN_FILE, markdown)
//        val builder = AnnotatedString.Builder()
//        builder.pushStyle(style.toSpanStyle())
//        builder.appendMarkdownAst(parsedTree, markdown, linkColor, listNestingLevel = 0)
//        builder.pop()
//        val result = builder.toAnnotatedString()
//        if (result.text.endsWith("\n")) {
//            result.subSequence(0, result.length - 1)
//        } else {
//            result
//        }
//    }
//
//    Text(
//        text = annotatedString,
//        style = style,
//        modifier = modifier
//    )
//}
//
//private fun AnnotatedString.Builder.appendMarkdownAst(
//    node: ASTNode,
//    markdown: String,
//    linkColor: Color,
//    listNestingLevel: Int = 0
//) {
//    when (node.type) {
//        MarkdownElementTypes.ATX_1 -> {
//            withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.ATX_2 -> {
//            withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.ATX_3 -> {
//            withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.ATX_4 -> {
//            withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.ATX_5 -> {
//            withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.ATX_6 -> {
//            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.SETEXT_1 -> {
//            withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.SETEXT_2 -> {
//            withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.PARAGRAPH -> {
//            if (listNestingLevel > 0) append("  ".repeat(listNestingLevel))
//            appendChildren(node, markdown, linkColor, listNestingLevel)
//            append("\n\n")
//        }
//        MarkdownElementTypes.STRONG -> {
//            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//        }
//        MarkdownElementTypes.EMPH -> {
//            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//        }
//        MarkdownElementTypes.INLINE_LINK -> {
//            val linkTextNode = node.children.find { it.type == MarkdownElementTypes.LINK_TEXT }
//            if (linkTextNode != null) {
//                withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
//                    appendChildren(linkTextNode, markdown, linkColor, listNestingLevel)
//                }
//            }
//        }
//        MarkdownElementTypes.AUTOLINK -> {
//            withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
//                append(node.getTextInNode(markdown).toString())
//            }
//        }
//        MarkdownElementTypes.CODE_SPAN -> {
//            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f))) {
//                append(node.children.getOrNull(1)?.getTextInNode(markdown)?.toString().orEmpty())
//            }
//        }
//        MarkdownElementTypes.CODE_BLOCK -> {
//            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f))) {
//                append(node.getTextInNode(markdown).toString())
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.BLOCK_QUOTE -> {
//            withStyle(SpanStyle(color = Color.Gray)) {
//                appendChildren(node, markdown, linkColor, listNestingLevel)
//            }
//            append("\n")
//        }
//        MarkdownElementTypes.UNORDERED_LIST -> {
//            node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.forEach { listItem ->
//                append("  ".repeat(listNestingLevel) + "•")
//                append("")
//                appendChildren(listItem, markdown, linkColor, listNestingLevel + 1)
//            }
//        }
//        MarkdownElementTypes.ORDERED_LIST -> {
//            node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.forEachIndexed { index, listItem ->
//                append("  ".repeat(listNestingLevel) + "${index + 1}.")
//                append("")
//                appendChildren(listItem, markdown, linkColor, listNestingLevel + 1)
//            }
//        }
//        MarkdownTokenTypes.TEXT, MarkdownTokenTypes.WHITE_SPACE -> {
//            append(node.getTextInNode(markdown).toString())
//        }
//        else -> {
//            appendChildren(node, markdown, linkColor, listNestingLevel)
//        }
//    }
//}
//
//private fun AnnotatedString.Builder.appendChildren(
//    node: ASTNode,
//    markdown: String,
//    linkColor: Color,
//    listNestingLevel: Int
//) {
//    node.children.forEach { child ->
//        appendMarkdownAst(child, markdown, linkColor, listNestingLevel)
//    }
//}