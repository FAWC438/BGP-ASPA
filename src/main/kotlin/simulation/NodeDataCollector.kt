package simulation

import bgp.notifications.BGPNotifier
import bgp.notifications.ExportListener
import bgp.notifications.ExportNotification
import core.simulator.notifications.*
import io.NodeDataReporter
import java.io.File
import java.io.IOException

/**
 *
 * 节点数据收集器收集与拓扑中每个单独节点相关的数据。
 */
class NodeDataCollector(private val reporter: NodeDataReporter) :
    DataCollector, StartListener, EndListener, ExportListener {

    /**
     * 创建一个将结果输出到指定输出文件的 Basic Reporter。
     */
    constructor(outputFile: File) : this(NodeDataReporter(outputFile))

    private val data = NodeDataSet()

    /**
     * 将收集器添加为收集器需要侦听以收集数据的通知的侦听器。
     */
    override fun register() {
        Notifier.addStartListener(this)
        Notifier.addEndListener(this)
        BGPNotifier.addExportListener(this)
    }

    /**
     * 从所有通知器中删除收集器
     */
    override fun unregister() {
        Notifier.removeStartListener(this)
        Notifier.removeEndListener(this)
        BGPNotifier.removeExportListener(this)
    }

    /**
     * 报告当前收集的数据。
     *
     * @throws IOException 如果发生 IO 错误
     */
    @Throws(IOException::class)
    override fun report() {
        reporter.report(data)
    }

    /**
     * 清除所有收集的数据。
     */
    override fun clear() {
        data.clear()
    }

    /**
     * 调用以通知侦听器新的开始通知。
     */
    override fun onStart(notification: StartNotification) {
        // 确保所有节点都以终止时间为 0 开始。
        // 这也确保所有节点都包含在终止时间映射中。
        // 为什么需要这样做？
        // 可能会出现某些节点从不导出路由的情况。如果是这种情况，那么这些节点将不会包含在终止时间映射中。
        for (node in notification.topology.nodes)
            data.terminationTimes[node.id] = 0
    }

    /**
     * 调用以通知侦听器新的导出通知。
     */
    override fun onExport(notification: ExportNotification) {
        // 更新导出新路由的节点的终止时间
        data.terminationTimes[notification.node.id] = notification.time
    }

    /**
     * 调用以通知侦听器新的结束通知。
     */
    override fun onEnd(notification: EndNotification) {
        for (node in notification.topology.nodes)
            data.selectedRoutes[node.id] = node.protocol.selectedRoute
    }

}