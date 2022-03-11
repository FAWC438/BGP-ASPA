package simulation

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * 数据收集器组处理多个收集器。它提供与数据收集器实现相同的接口。因此，它可以用作任何其他数据收集器。
 */
class DataCollectorGroup : DataCollector {

    private val collectors = mutableListOf<DataCollector>()

    /**
     * 向组中添加一个新的收集器。
     */
    fun add(collector: DataCollector) {
        collectors.add(collector)
    }

    /**
     * 注册组中的所有收集器。
     */
    override fun register() {
        collectors.forEach { it.register() }
    }

    /**
     * 取消注册组中的所有收集器。
     */
    override fun unregister() {
        collectors.forEach { it.unregister() }
    }

    /**
     * 报告从组中所有收集器收集的数据。
     */
    override fun report() {
        collectors.forEach { it.report() }
    }

    /**
     * 清除组中所有收集器的所有数据。
     */
    override fun clear() {
        collectors.forEach { it.clear() }
    }

}