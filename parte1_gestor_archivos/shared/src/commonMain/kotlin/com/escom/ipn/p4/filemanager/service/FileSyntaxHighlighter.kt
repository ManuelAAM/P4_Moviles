package com.escom.ipn.p4.filemanager.service

enum class SyntaxTokenType {
    PLAIN,
    KEY,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    PUNCTUATION,
    XML_TAG,
    XML_ATTR_NAME,
    XML_ATTR_VALUE,
    XML_TEXT,
    COMMENT
}

data class SyntaxToken(
    val text: String,
    val type: SyntaxTokenType
)

object FileSyntaxHighlighter {

    /**
     * Formatea un texto JSON y devuelve tokens etiquetados para resaltado de sintaxis
     */
    fun highlightJson(rawJson: String): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        val length = rawJson.length
        var i = 0

        while (i < length) {
            val c = rawJson[i]

            when {
                c.isWhitespace() -> {
                    val start = i
                    while (i < length && rawJson[i].isWhitespace()) {
                        i++
                    }
                    tokens.add(SyntaxToken(rawJson.substring(start, i), SyntaxTokenType.PLAIN))
                }
                c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',' -> {
                    tokens.add(SyntaxToken(c.toString(), SyntaxTokenType.PUNCTUATION))
                    i++
                }
                c == '"' -> {
                    val start = i
                    i++
                    while (i < length && rawJson[i] != '"') {
                        if (rawJson[i] == '\\' && i + 1 < length) {
                            i++
                        }
                        i++
                    }
                    if (i < length && rawJson[i] == '"') {
                        i++
                    }
                    val strValue = rawJson.substring(start, i)
                    // Check if it's a key (followed by colon after whitespace)
                    var j = i
                    while (j < length && rawJson[j].isWhitespace()) {
                        j++
                    }
                    val isKey = (j < length && rawJson[j] == ':')
                    tokens.add(SyntaxToken(strValue, if (isKey) SyntaxTokenType.KEY else SyntaxTokenType.STRING))
                }
                c.isDigit() || c == '-' -> {
                    val start = i
                    while (i < length && (rawJson[i].isDigit() || rawJson[i] == '.' || rawJson[i] == 'e' || rawJson[i] == 'E' || rawJson[i] == '+' || rawJson[i] == '-')) {
                        i++
                    }
                    tokens.add(SyntaxToken(rawJson.substring(start, i), SyntaxTokenType.NUMBER))
                }
                c.isLetter() -> {
                    val start = i
                    while (i < length && rawJson[i].isLetter()) {
                        i++
                    }
                    val word = rawJson.substring(start, i)
                    val type = when (word) {
                        "true", "false" -> SyntaxTokenType.BOOLEAN
                        "null" -> SyntaxTokenType.NULL
                        else -> SyntaxTokenType.PLAIN
                    }
                    tokens.add(SyntaxToken(word, type))
                }
                else -> {
                    tokens.add(SyntaxToken(c.toString(), SyntaxTokenType.PLAIN))
                    i++
                }
            }
        }
        return tokens
    }

    /**
     * Formatea un texto XML y devuelve tokens etiquetados para resaltado de sintaxis
     */
    fun highlightXml(rawXml: String): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        val length = rawXml.length
        var i = 0

        while (i < length) {
            val c = rawXml[i]

            if (c == '<') {
                if (i + 3 < length && rawXml.substring(i, i + 4) == "<!--") {
                    val endComment = rawXml.indexOf("-->", i + 4)
                    if (endComment != -1) {
                        tokens.add(SyntaxToken(rawXml.substring(i, endComment + 3), SyntaxTokenType.COMMENT))
                        i = endComment + 3
                        continue
                    }
                }

                val tagEnd = rawXml.indexOf('>', i)
                if (tagEnd != -1) {
                    val tagContent = rawXml.substring(i, tagEnd + 1)
                    parseXmlTagTokens(tagContent, tokens)
                    i = tagEnd + 1
                    continue
                } else {
                    tokens.add(SyntaxToken(rawXml.substring(i), SyntaxTokenType.PLAIN))
                    break
                }
            } else {
                val nextTag = rawXml.indexOf('<', i)
                val textEnd = if (nextTag != -1) nextTag else length
                val text = rawXml.substring(i, textEnd)
                tokens.add(SyntaxToken(text, SyntaxTokenType.XML_TEXT))
                i = textEnd
            }
        }
        return tokens
    }

    private fun parseXmlTagTokens(tag: String, tokens: MutableList<SyntaxToken>) {
        var i = 0
        val len = tag.length

        // Apertura < o </ o <?
        if (tag.startsWith("</") || tag.startsWith("<?")) {
            tokens.add(SyntaxToken(tag.substring(0, 2), SyntaxTokenType.PUNCTUATION))
            i = 2
        } else {
            tokens.add(SyntaxToken("<", SyntaxTokenType.PUNCTUATION))
            i = 1
        }

        // Nombre de la etiqueta
        val nameStart = i
        while (i < len && (tag[i].isLetterOrDigit() || tag[i] == ':' || tag[i] == '-' || tag[i] == '_')) {
            i++
        }
        if (i > nameStart) {
            tokens.add(SyntaxToken(tag.substring(nameStart, i), SyntaxTokenType.XML_TAG))
        }

        // Atributos
        while (i < len) {
            val c = tag[i]
            when {
                c.isWhitespace() -> {
                    val wsStart = i
                    while (i < len && tag[i].isWhitespace()) i++
                    tokens.add(SyntaxToken(tag.substring(wsStart, i), SyntaxTokenType.PLAIN))
                }
                c == '/' && i + 1 < len && tag[i + 1] == '>' -> {
                    tokens.add(SyntaxToken("/>", SyntaxTokenType.PUNCTUATION))
                    i += 2
                }
                c == '?' && i + 1 < len && tag[i + 1] == '>' -> {
                    tokens.add(SyntaxToken("?>", SyntaxTokenType.PUNCTUATION))
                    i += 2
                }
                c == '>' -> {
                    tokens.add(SyntaxToken(">", SyntaxTokenType.PUNCTUATION))
                    i++
                }
                c == '=' -> {
                    tokens.add(SyntaxToken("=", SyntaxTokenType.PUNCTUATION))
                    i++
                }
                c == '"' || c == '\'' -> {
                    val quote = c
                    val startQuote = i
                    i++
                    while (i < len && tag[i] != quote) i++
                    if (i < len && tag[i] == quote) i++
                    tokens.add(SyntaxToken(tag.substring(startQuote, i), SyntaxTokenType.XML_ATTR_VALUE))
                }
                tag[i].isLetterOrDigit() || tag[i] == ':' || tag[i] == '-' -> {
                    val attrStart = i
                    while (i < len && (tag[i].isLetterOrDigit() || tag[i] == ':' || tag[i] == '-' || tag[i] == '_')) i++
                    tokens.add(SyntaxToken(tag.substring(attrStart, i), SyntaxTokenType.XML_ATTR_NAME))
                }
                else -> {
                    tokens.add(SyntaxToken(c.toString(), SyntaxTokenType.PLAIN))
                    i++
                }
            }
        }
    }
}
