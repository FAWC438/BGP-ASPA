package io

import core.routing.NodeID
import utils.toNonNegativeInt
import java.io.Closeable
import java.io.IOException
import java.io.Reader
import java.util.*

class TopologyParser(reader: Reader) : Closeable {

    /**
     * Handlers are notified once a new topology item (a node or a link) is parsed.
     * Subclasses should implement how these items should be handled.
     */
    interface Handler {

        /**
         * 在读取新节点项时读取流时调用。
         *
         * @param id          节点解析的ID
         * @param values      与节点关联的值序列
         * @param currentLine 解析节点的行号
         */
        fun onNodeItem(id: NodeID, values: List<String>, currentLine: Int)

        /**
         * 在读取新链接项时读取流时调用。
         *
         * @param tail        尾节点ID
         * @param head        头节点ID
         * @param values      与链接关联的值序列
         * @param currentLine 解析节点的行号
         */
        fun onLinkItem(tail: NodeID, head: NodeID, values: List<String>, currentLine: Int)

    }

    private class KeyValueHandler(val handler: Handler) : KeyValueParser.Handler {

        /**
         * 解析新条目时调用。
         *
         * @param entry       解析的条目
         * @param currentLine 解析节点的行号
         */
        override fun onEntry(entry: KeyValueParser.Entry, currentLine: Int) {

            val values = entry.values

            when (entry.key.lowercase(Locale.getDefault())) {
                "node" -> {

                    // 第一个值是节点 ID - 这个值是强制性的
                    if (values.isEmpty() || (values.size == 1 && values[0].isEmpty())) {
                        throw ParseException("node entry is missing the ID value", currentLine)
                    }

                    val nodeID = parseNodeID(values[0], currentLine)

                    // 其余值应由拓扑阅读器根据其所需规范进行解析
                    handler.onNodeItem(nodeID, values.subList(1, values.lastIndex + 1), currentLine)
                }
                "link" -> {

                    // The first two values are the tail and head nodes of the link
                    if (values.size < 2 || values[0].isBlank() || values[1].isBlank()) {
                        throw ParseException(
                            "link entry is missing required values: tail node ID and/or head node ID",
                            currentLine
                        )
                    }

                    val tailID = parseNodeID(values[0], currentLine)
                    val headID = parseNodeID(values[1], currentLine)

                    handler.onLinkItem(tailID, headID, values.subList(2, values.lastIndex + 1), currentLine)
                }
                else -> {
                    throw ParseException("invalid key `${entry.key}`: supported keys are 'node' or 'link'", currentLine)
                }

            }
        }

        private fun parseNodeID(value: String, currentLine: Int): Int {

            try {
                return value.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("a node ID must be a non-negative value, but was `$value`", currentLine)
            }
        }
    }

    /**
     * 拓扑解析器基于键值解析器。它使用这个解析器来处理识别条目。
     */
    private val parser = KeyValueParser(reader)

    /**
     * 解析新节点或链接后，解析调用处理程序的流。
     *
     * @throws IOException    如果发生 IO 错误
     * @throws ParseException 如果由于不正确的表示而无法创建拓扑对象
     */
    @Throws(IOException::class, ParseException::class)
    fun parse(handler: Handler) {
        parser.parse(KeyValueHandler(handler))
    }

    /**
     * 关闭流并释放与其关联的任何系统资源。
     */
    override fun close() {
        parser.close()
    }
}
