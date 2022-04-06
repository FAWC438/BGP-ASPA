package io

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.Reader

/**
 *
 * 用于键值格式流的解析器。
 *
 * 键值格式的流有多个条目（每行一个），格式如下：
 *
 *   key = value1 | value2 | ... | valueN
 *
 * 用等号“=”将键与值分开。
 * 一个键可以与多个值相关联。每一个都用'|'隔开特点。
 *
 * 每个解析器都与一个处理程序相关联。每次从流中解析新条目时都会通知此处理程序。然后处理程序负责解析键和值，确保它们根据所需的规范有效。
 *
 * 条目按照它们在流中描述的相同顺序进行解析。因此，保证处理程序以完全相同的顺序被通知新条目。
 */
class KeyValueParser(reader: Reader) : Closeable {

    /**
     * 一旦解析了新的键值条目，就会通知处理程序。
     *
     * 子类应该使用这种方法来解析键和值，确保它们根据它们的唯一规范是有效的。
     */
    interface Handler {

        /**
         * 解析新条目时调用。
         *
         * @param entry       解析的条目
         * @param currentLine 解析节点的行号
         */
        fun onEntry(entry: Entry, currentLine: Int)

    }

    data class Entry(val key: String, val values: List<String>)

    /**
     * 用于读取流的底层读取器。
     */
    private val reader = BufferedReader(reader)

    /**
     * 解析流，每次解析新条目时调用处理程序。
     *
     * @param handler 解析新条目时通知的处理程序
     * @throws IOException    如果发生 IO 错误
     * @throws ParseException 如果流的格式无效
     */
    @Throws(IOException::class, ParseException::class)
    fun parse(handler: Handler) {

        // 阅读第一行 - 如果为空则抛出错误
        var line: String? = reader.readLine() ?: throw ParseException("file is empty", lineNumber = 1)

        var currentLine = 1
        while (line != null) {

            // 忽略空行
            if (line.isNotBlank()) {
                val entry = parseEntry(line, currentLine)
                handler.onEntry(entry, currentLine)
            }

            line = reader.readLine()
            currentLine++
        }
    }

    private fun parseEntry(line: String, currentLine: Int): Entry {

        // 每行必须有一个键与它的值用等号分隔
        // e.g. node = 1

        // 从值中拆分键
        val keyAndValues = line.split("=", limit = 2)

        if (keyAndValues.size < 2) {
            throw ParseException(
                "line $currentLine$ is missing an equal sign '=' to " +
                        "distinguish between key and values", currentLine
            )
        }

        val key = keyAndValues[0].trim()
        val values = keyAndValues[1].split("|").map { it.trim() }.toList()

        return Entry(key, values)
    }

    /**
     * 关闭流并释放与其关联的任何系统资源。
     */
    override fun close() {
        reader.close()
    }
}