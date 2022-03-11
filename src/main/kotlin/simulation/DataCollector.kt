package simulation

/**
 *
 * 数据收集器负责在执行期间收集数据。
 *
 * 任何方法都可以用来收集它。最重要的方法之一是在模拟过程中使用模拟器发出的通知。为此，请查看使用 Notifier 接口标记的通知程序类。
 */
interface DataCollector {

    /**
     * 执行数据收集的辅助方法。它使用必要的通知器处理注册和取消注册收集器。
     */
    fun collect(body: () -> Unit): DataCollector {

        register()
        try {
            body()
        } finally {
            unregister()
        }

        return this
    }

    /**
     * 将收集器添加为收集器需要侦听以收集数据的通知的侦听器。
     */
    fun register()

    /**
     * 从所有通知器中删除收集器
     */
    fun unregister()

    /**
     * 报告当前收集的数据。
     */
    fun report()

    /**
     * 清除所有收集的数据。
     */
    fun clear()

}