package simulation

import bgp.notifications.*
import core.simulator.notifications.Notifier
import core.simulator.notifications.StartListener
import core.simulator.notifications.StartNotification
import io.pretty
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileWriter

/**
 *
 * TODO @doc
 * TODO @optimization - try different methods of writing that may speedup the simulation process
 */
class TraceReporter(outputFile: File) : DataCollector, StartListener,
    LearnListener, ExportListener, SelectListener, DetectListener, Closeable {

    private val baseOutputFile = outputFile

    /**
     * 报告器将每个模拟的跟踪输出到其自己的文件中。
     * 此变量存储当前仿真的输出文件。每次新的模拟开始时都会更新。
     */
    private var simulationWriter: BufferedWriter? = null
    private var simulationNumber = 0

    /**
     * 存储“节点”列的大小。这取决于最长节点 ID 的大小。
     * 默认情况下，它设置为适合标题中包含的“节点”一词。
     */
    private var nodeColumnSize = 4

    /**
     * 将收集器添加为收集器需要侦听以收集数据的通知的侦听器。
     */
    override fun register() {
        Notifier.addStartListener(this)
        BGPNotifier.addLearnListener(this)
        BGPNotifier.addExportListener(this)
        BGPNotifier.addSelectListener(this)
        BGPNotifier.addDetectListener(this)
    }

    /**
     * 从所有通知器中删除收集器
     */
    override fun unregister() {
        Notifier.removeStartListener(this)
        BGPNotifier.removeLearnListener(this)
        BGPNotifier.removeExportListener(this)
        BGPNotifier.removeSelectListener(this)
        BGPNotifier.removeDetectListener(this)

        // 在丢弃跟踪报告器之前必须始终调用取消注册
        // 因此，这是确保当前编写器关闭的一种方法
        close()
    }

    /**
     * 关闭底层编写器。
     */
    override fun close() {
        simulationWriter?.close()
        simulationWriter = null
    }

    /**
     * 报告当前收集的数据。
     */
    override fun report() {
        // nothing to do
        // reporting is done during the execution
    }

    /**
     * 清除所有收集的数据。
     */
    override fun clear() {
        // nothing to do
    }

    /**
     * 调用以通知侦听器新的开始通知。
     */
    override fun onStart(notification: StartNotification) {
        // 跟踪模拟的数量对于确保新模拟的跟踪输出不会覆盖前一个模拟非常重要
        simulationNumber++

        // 每个模拟的跟踪输出都写入自己的文件
        val simulationOutputFile = File(
            baseOutputFile.parent, baseOutputFile.nameWithoutExtension +
                    "$simulationNumber.${baseOutputFile.extension}"
        )

        // 关闭用于先前模拟的编写器并为新模拟创建一个新的
        simulationWriter?.close()
        simulationWriter = BufferedWriter(FileWriter(simulationOutputFile))

        // 查找所有节点 ID 以确定哪个节点的 ID 号最长
        // val maxIDSize = notification.topology.nodes.asSequence().map { it.id }.maxOrNull() ?: 0

        // 节点列大小对应于单词“Node”和最长节点 ID 之间的最长
        // warning: 此处有重大bug
        // nodeColumnSize = maxOf(4, maxIDSize)
        nodeColumnSize = 8

        // 写标题
        simulationWriter?.apply {
            write("${align("Time")}| Event  | ${align("Node", nodeColumnSize)} | Routing Information\n")
        }
    }

    /**
     * 调用以通知侦听器新的学习通知。
     */
    override fun onLearn(notification: LearnNotification) {
        simulationWriter?.apply {
            notification.apply {
                write(
                    "${align(time)}| LEARN  | ${align(node.pretty(), nodeColumnSize)} | ${route.pretty()} " +
                            "via ${neighbor.pretty()}\n"
                )
            }
        }
    }

    /**
     * 调用以通知侦听器新的导出通知。
     */
    override fun onExport(notification: ExportNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| EXPORT | ${align(node.pretty(), nodeColumnSize)} | ${route.pretty()}\n")
            }
        }
    }

    /**
     * 调用以通知侦听器新的学习通知。
     */
    override fun onSelect(notification: SelectNotification) {
        simulationWriter?.apply {
            notification.apply {
                write(
                    "${align(time)}| SELECT | ${align(node.pretty(), nodeColumnSize)} | " +
                            "${selectedRoute.pretty()} over ${previousRoute.pretty()}\n"
                )
            }
        }
    }

    /**
     * 调用以通知侦听器新的检测通知。
     */
    override fun onDetect(notification: DetectNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| DETECT | ${align(node.pretty(), nodeColumnSize)} |\n")
            }
        }
    }

    //
    //  帮助对齐消息中显示的信息的辅助函数
    //

    private fun align(value: Any, length: Int = 7): String {

        val builder = StringBuilder(length)

        val text = value.toString()
        val remainder = length - text.length
        val padding = remainder / 2

        // Add padding to the left
        for (i in 1..(padding))
            builder.append(' ')

        // Add the text at the center
        builder.append(text)

        // Add padding to the right
        for (i in 1..(padding + remainder % 2))
            builder.append(' ')

        return builder.toString()
    }

}