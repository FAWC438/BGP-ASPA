package simulation

/**
 * 包含构成模拟运行元数据的所有键和值对的容器。
 */
class Metadata(version: String) : Iterable<Pair<String, Any>> {

    private val data = LinkedHashMap<String, Any>()

    init {
        data["Version"] = version
    }

    operator fun set(key: String, value: Any) {
        data[key] = value
    }

    operator fun get(key: String): Any? = data[key]

    // 基于地图的迭代器的迭代器。它将条目转换为键值对
    private class MetadataIterator(private val iterator: MutableIterator<MutableMap.MutableEntry<String, Any>>) :
        Iterator<Pair<String, Any>> {

        /**
         * 如果迭代有更多元素，则返回 `true`。
         */
        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        /**
         * 返回迭代中的下一个元素。
         */
        override fun next(): Pair<String, Any> {
            val (key, value) = iterator.next()
            return Pair(key, value)
        }

    }

    /**
     * 返回此对象的元素的迭代器。
     */
    override fun iterator(): Iterator<Pair<String, Any>> {
        return MetadataIterator(data.iterator())
    }
}
