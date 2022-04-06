package io

import bgp.BGPRoute
import core.routing.pathOf
import utils.toNonNegativeInt
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader


private const val DEFAULT_ADVERTISING_TIME = 0
private val DEFAULT_DEFAULT_ROUTE = BGPRoute.self()


class InterdomainAdvertisementReader(reader: Reader) : AutoCloseable {

    constructor(file: File) : this(FileReader(file))

    private class Handler(val advertisements: MutableList<AdvertisementInfo<BGPRoute>>) : KeyValueParser.Handler {

        /**
         * 解析新条目时调用。
         *
         * @param entry       解析的条目
         * @param currentLine 解析节点的行号
         */
        override fun onEntry(entry: KeyValueParser.Entry, currentLine: Int) {

            if (entry.values.size > 2) {
                throw ParseException(
                    "only 2 values are expected for an advertiser, " +
                            "but ${entry.values.size} were given", currentLine
                )
            }

            // 键对应广告客户 ID
            val advertiserID = try {
                entry.key.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException(
                    "advertising node ID must be a non-negative integer value, " +
                            "but was '${entry.key}'", currentLine
                )
            }

            // 第一个值是广告时间 - 这个值不是强制性的
            // KeyValueParser 确保始终存在至少一个值，即使它是空白的
            val timeValue = entry.values[0]
            val time = if (timeValue.isBlank()) DEFAULT_ADVERTISING_TIME else try {
                timeValue.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException(
                    "advertising time must be a non-negative integer value, " +
                            "but was '$timeValue'", currentLine
                )
            }

            // 第二个值是默认路由的本地首选项的成本标签 - 此值不是强制性的
            val defaultRoute = if (entry.values.size == 1 || entry.values[1].isBlank()) {
                DEFAULT_DEFAULT_ROUTE
            } else {
                BGPRoute.with(parseInterdomainCost(entry.values[1], currentLine), pathOf())
            }

            advertisements.add(AdvertisementInfo(advertiserID, defaultRoute, time))
        }

    }

    private val parser = KeyValueParser(reader)

    /**
     * 读取地图，将广告商 ID 映射到成对的默认路由和广告时间。
     *
     * @throws ParseException 如果输入的格式无效
     * @throws IOException 如果发生 IO 错误
     * @return 从流中读取的广告列表
     */
    @Throws(ParseException::class, IOException::class)
    fun read(): List<AdvertisementInfo<BGPRoute>> {
        val advertisements = ArrayList<AdvertisementInfo<BGPRoute>>()
        parser.parse(Handler(advertisements))
        return advertisements
    }

    /**
     * 关闭底层流。
     */
    override fun close() {
        parser.close()
    }
}
