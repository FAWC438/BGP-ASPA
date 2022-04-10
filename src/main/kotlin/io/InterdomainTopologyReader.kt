package io

import bgp.*
import core.routing.*
import core.simulator.Time
import utils.toNonNegativeInt
import java.io.*
import java.util.*

class InterdomainTopologyReader(reader: Reader, private val forcedMRAI: Time? = null) : TopologyReader<BGPRoute>,
    Closeable {

    /**
     * 提供使用文件对象创建阅读器的选项。
     */
    @Throws(FileNotFoundException::class)
    constructor(file: File, forcedMRAI: Time? = null) : this(FileReader(file), forcedMRAI)

    private val parser = TopologyParser(reader)

    private inner class InterdomainHandler(val builder: TopologyBuilder<BGPRoute>) : TopologyParser.Handler {

        /**
         * 在读取新节点项时读取流时调用。
         *
         * @param id          节点解析的ID
         * @param values      与节点关联的值序列
         * @param currentLine 解析节点的行号
         */
        override fun onNodeItem(id: NodeID, values: List<String>, currentLine: Int) {

            if (values.size < 2) {
                throw ParseException("node is missing required values: Protocol and/or MRAI", currentLine)
            }

            val protocolLabel = values[0]

            // 如果设置，请使用“强制 MRAI”值。否则，使用拓扑文件中指定的值。
            val mrai = forcedMRAI ?: try {
                values[1].toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("MRAI must be a non-negative integer number, but was ${values[1]}", currentLine)
            }

            // TODO @refactor - make this case sensitive
            val protocol = when (protocolLabel.lowercase(Locale.getDefault())) {
                "bgp" -> BGP(mrai)
                "attack1" -> Attacker(mrai, aType = 1)
                "attack2" -> Attacker(mrai, aType = 2)
                "attack3" -> Attacker(mrai, aType = 3)
                "aspa" -> ASPA(mrai)
                "ssbgp" -> SSBGP(mrai)
                "issbgp" -> ISSBGP(mrai)
                "ssbgp2" -> SSBGP2(mrai)
                "issbgp2" -> ISSBGP2(mrai)
                else -> throw ParseException(
                    "protocol label `$protocolLabel` was not recognized ", currentLine
                )
            }

            try {
                builder.addNode(id, protocol)

            } catch (e: ElementExistsException) {
                throw ParseException(e.message!!, currentLine)
            }
        }

        /**
         * 在读取新链接项时读取流时调用。
         *
         * @param tail        尾节点ID
         * @param head        头节点ID
         * @param values      与链接项关联的值序列
         * @param currentLine 解析节点的行号
         */
        override fun onLinkItem(tail: NodeID, head: NodeID, values: List<String>, currentLine: Int) {

            if (values.isEmpty()) {
                throw ParseException("link is missing extender label value", currentLine)
            }

            val extender = parseInterdomainExtender(values[0], currentLine)

            try {
                builder.link(tail, head, extender)

            } catch (e: ElementNotFoundException) {
                throw ParseException(e.message!!, currentLine)
            } catch (e: ElementExistsException) {
                throw ParseException(e.message!!, currentLine)
            }
        }
    }

    /**
     * 返回在输入源中表示的拓扑对象。
     *
     * 拓扑对象使用类似 BGP 的协议，分配给链路的扩展器在域间路由策略中定义。
     *
     * @throws IOException    如果发生 IO 错误
     * @throws ParseException 如果由于不正确的表示而无法创建拓扑对象
     */
    @Throws(IOException::class, ParseException::class)
    override fun read(): Topology<BGPRoute> {
        val builder = TopologyBuilder<BGPRoute>()
        parser.parse(InterdomainHandler(builder))
        return builder.build()
    }

    /**
     * 关闭流并释放与其关联的任何系统资源。
     */
    override fun close() {
        parser.close()
    }
}