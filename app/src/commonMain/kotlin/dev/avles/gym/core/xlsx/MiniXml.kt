package dev.avles.gym.core.xlsx

/** XML this app will not read, with a reason. */
class XmlException(message: String) : Exception(message)

/**
 * One element: its local name (namespace prefix dropped, so `x:row` is `row`),
 * attributes by local name, child elements, and its own text.
 */
class XmlNode(val name: String, val attrs: Map<String, String>, val children: List<XmlNode>, val text: String) {
    fun child(name: String): XmlNode? = children.firstOrNull { it.name == name }
    fun all(name: String): List<XmlNode> = children.filter { it.name == name }

    /** Every descendant called [name], in document order. */
    fun descendants(name: String): List<XmlNode> {
        val found = ArrayList<XmlNode>()
        fun walk(n: XmlNode) { n.children.forEach { if (it.name == name) found += it; walk(it) } }
        walk(this)
        return found
    }
}

/**
 * The smallest XML parser that reads what spreadsheet apps write.
 *
 * Hand-written on purpose. Platform XML parsers resolve DTDs and entities, which is
 * how "XML external entity" and "billion laughs" attacks work; this one refuses any
 * DOCTYPE outright and knows only the five predefined entities and numeric
 * character references, so a file can never make it fetch, expand or execute
 * anything. Depth and element count are capped so a pathological file fails with
 * an [XmlException] instead of exhausting memory or the stack.
 */
object MiniXml {

    private const val MAX_DEPTH = 64
    private const val MAX_NODES = 600_000

    fun parse(text: String): XmlNode = Parser(text).document()

    private class Builder(val name: String, val attrs: Map<String, String>) {
        val children = ArrayList<XmlNode>()
        val text = StringBuilder()
        fun build() = XmlNode(name, attrs, children, text.toString())
    }

    private class Parser(private val s: String) {
        private var i = 0
        private var nodes = 0

        fun document(): XmlNode {
            val stack = ArrayList<Builder>()
            var root: XmlNode? = null
            while (i < s.length) {
                val lt = s.indexOf('<', i)
                val textEnd = if (lt < 0) s.length else lt
                if (textEnd > i) {
                    val raw = s.substring(i, textEnd)
                    if (stack.isNotEmpty()) stack.last().text.append(unescape(raw))
                    else if (raw.isNotBlank()) throw XmlException("Text outside the document.")
                }
                if (lt < 0) break
                i = lt
                when {
                    s.startsWith("<?", i) -> i = after("?>")
                    s.startsWith("<!--", i) -> i = after("-->")
                    s.startsWith("<![CDATA[", i) -> {
                        val end = s.indexOf("]]>", i + 9)
                        if (end < 0) throw XmlException("Unclosed CDATA.")
                        if (stack.isNotEmpty()) stack.last().text.append(s, i + 9, end)
                        i = end + 3
                    }
                    s.startsWith("<!", i) -> throw XmlException("The file declares a DOCTYPE or entity, which is not allowed.")
                    s.startsWith("</", i) -> {
                        val end = s.indexOf('>', i)
                        if (end < 0) throw XmlException("Unclosed tag.")
                        val name = localName(s.substring(i + 2, end).trim())
                        val open = stack.removeLastOrNull() ?: throw XmlException("Unexpected </$name>.")
                        if (open.name != name) throw XmlException("Mismatched </$name>.")
                        val node = open.build()
                        if (stack.isEmpty()) { if (root != null) throw XmlException("Two root elements."); root = node }
                        else stack.last().children += node
                        i = end + 1
                    }
                    else -> {
                        val (builder, selfClosing) = openTag()
                        if (++nodes > MAX_NODES) throw XmlException("The sheet is too large.")
                        if (selfClosing) {
                            val node = builder.build()
                            if (stack.isEmpty()) { if (root != null) throw XmlException("Two root elements."); root = node }
                            else stack.last().children += node
                        } else {
                            if (stack.size >= MAX_DEPTH) throw XmlException("The file nests too deeply.")
                            stack += builder
                        }
                    }
                }
            }
            if (stack.isNotEmpty()) throw XmlException("The file ends inside <${stack.last().name}>.")
            return root ?: throw XmlException("The part is empty.")
        }

        private fun after(token: String): Int {
            val end = s.indexOf(token, i)
            if (end < 0) throw XmlException("Unclosed $token.")
            return end + token.length
        }

        /** Reads `<name a="1" b='2'>` or `<name/>` starting at '<'. */
        private fun openTag(): Pair<Builder, Boolean> {
            i++ // '<'
            val nameStart = i
            while (i < s.length && !s[i].isWhitespace() && s[i] != '>' && s[i] != '/') i++
            val name = localName(s.substring(nameStart, i))
            if (name.isEmpty()) throw XmlException("A tag has no name.")
            val attrs = HashMap<String, String>()
            while (true) {
                while (i < s.length && s[i].isWhitespace()) i++
                if (i >= s.length) throw XmlException("Unclosed <$name>.")
                when (s[i]) {
                    '>' -> { i++; return Builder(name, attrs) to false }
                    '/' -> {
                        if (i + 1 >= s.length || s[i + 1] != '>') throw XmlException("Bad tag <$name>.")
                        i += 2
                        return Builder(name, attrs) to true
                    }
                    else -> {
                        val aStart = i
                        while (i < s.length && s[i] != '=' && !s[i].isWhitespace() && s[i] != '>') i++
                        val rawName = s.substring(aStart, i)
                        while (i < s.length && s[i].isWhitespace()) i++
                        if (i >= s.length || s[i] != '=') throw XmlException("Bad attribute in <$name>.")
                        i++
                        while (i < s.length && s[i].isWhitespace()) i++
                        if (i >= s.length || (s[i] != '"' && s[i] != '\'')) throw XmlException("Bad attribute in <$name>.")
                        val quote = s[i]
                        val end = s.indexOf(quote, i + 1)
                        if (end < 0) throw XmlException("Unclosed attribute in <$name>.")
                        val value = unescape(s.substring(i + 1, end))
                        i = end + 1
                        if (!rawName.startsWith("xmlns")) attrs[localName(rawName)] = value
                    }
                }
            }
        }

        private fun localName(qualified: String): String = qualified.substringAfter(':')

        private fun unescape(raw: String): String {
            if (raw.indexOf('&') < 0) return raw
            val out = StringBuilder(raw.length)
            var j = 0
            while (j < raw.length) {
                val c = raw[j]
                if (c != '&') { out.append(c); j++; continue }
                val semi = raw.indexOf(';', j)
                if (semi < 0 || semi - j > 10) throw XmlException("Bad entity.")
                val entity = raw.substring(j + 1, semi)
                when {
                    entity == "amp" -> out.append('&')
                    entity == "lt" -> out.append('<')
                    entity == "gt" -> out.append('>')
                    entity == "quot" -> out.append('"')
                    entity == "apos" -> out.append('\'')
                    entity.startsWith("#x") || entity.startsWith("#X") -> appendCode(out, entity.substring(2).toIntOrNull(16))
                    entity.startsWith("#") -> appendCode(out, entity.substring(1).toIntOrNull())
                    else -> throw XmlException("Unknown entity &$entity;.")
                }
                j = semi + 1
            }
            return out.toString()
        }

        private fun appendCode(out: StringBuilder, code: Int?) {
            if (code == null || code < 0 || code > 0x10FFFF) throw XmlException("Bad character reference.")
            if (code < 0x10000) out.append(code.toChar())
            else {
                val v = code - 0x10000
                out.append((0xD800 + (v shr 10)).toChar()).append((0xDC00 + (v and 0x3FF)).toChar())
            }
        }
    }
}
