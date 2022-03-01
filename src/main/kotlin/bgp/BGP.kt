package bgp

import bgp.notifications.*
import core.routing.*
import core.simulator.Time
import core.simulator.Timer

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
abstract class BaseBGP(val mrai: Time, routingTable: RoutingTable<BGPRoute>) : Protocol<BGPRoute> {

    /**
     * 包含候选路由的路由表。
     * 使用路由选择器执行路由选择。
     * 如果提供了路由表，则路由选择器会包装提供的路由表。否则，它会包装一个新的路由表。
     */
    val routingTable = RouteSelector.wrap(routingTable, ::bgpRouteCompare)

    /**
     * 协议选择的路由。
     */
    override val selectedRoute: BGPRoute
        get() = routingTable.getSelectedRoute()

    var mraiTimer = Timer.disabled()
        protected set

    /**
     * 指示是否由于处理新传入消息而选择了新路由的标志。
     * 当新消息到达时，此标志始终设置为 false，并且只有在处理消息时选择了新路由时才应设置为 true。
     */
    protected var wasSelectedRouteUpdated: Boolean = false

    /**
     * 存储导出到邻居的最后一条路线。
     * 这用于确保在 MRAI 到期时不会再次导出选定和导出的内容。
     */
    private var lastExportedRoute = BGPRoute.invalid()

    /**
     * 将 [route] 设置为 [node] 的本地路由。本地路由可能为从邻居学习到的任何其他候选路由。因此，此操作可能会导致导出新的路由。
     *
     * @param node  要为其设置本地路由的节点
     * @param route 设置为本地路由的路由
     */
    override fun setLocalRoute(node: Node<BGPRoute>, route: BGPRoute) {
        process(node, node, route)
    }

    /**
     * 处理节点接收到的 BGP 消息。
     * 可能会更新路由表和选定的路由邻居。
     *
     * @param message 要处理的消息
     */
    override fun process(message: Message<BGPRoute>) {
        process(message.recipient, message.sender, message.route)
    }

    /**
     * 处理节点引入的BGP路由。
     * 可能会更新路由表和选定的路由邻居。
     *
     * @param node          导入路由的节点
     * @param neighbor      导出路由的邻居
     * @param importedRoute [node]导入的路由
     */
    fun process(node: Node<BGPRoute>, neighbor: Node<BGPRoute>, importedRoute: BGPRoute) {

        // 存储节点在处理此消息之前选择的路由
        val previousSelectedRoute = routingTable.getSelectedRoute()

        val learnedRoute = learn(node, neighbor, importedRoute)
        BGPNotifier.notify(LearnNotification(node, learnedRoute, neighbor))

        val updated = routingTable.update(neighbor, learnedRoute)

        // 如果“更新”为真或保持其当前状态，则将更新标志设置为真
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        if (wasSelectedRouteUpdated) {

            val selectedRoute = routingTable.getSelectedRoute()
            BGPNotifier.notify(
                SelectNotification(node, selectedRoute, previousSelectedRoute)
            )

            export(node)
            wasSelectedRouteUpdated = false
        }
    }

    /**
     * 实现学习路线的过程。
     *
     * @param node   处理路由的节点
     * @param sender 发送路由的邻居
     * @param route  节点导入的路由（应用扩展器后得到的路由）
     * @return 如果路由的 AS-PATH 不包含学习路由的节点，则返回导入的路由；如果路由的 AS-PATH 包含学习节点，则返回“无效”。
     * 请注意，如果导入的路由无效，它也可能返回无效路由。
     */
    protected fun learn(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute): BGPRoute {

        if (node in route.asPath) {
            // 通知 implementations 检测到循环
            onLoopDetected(node, sender, route)

            return BGPRoute.invalid()
        } else {
            return route
        }
    }

    /**
     * 实现导出路由的过程。它导出节点当前选择的路由。
     *
     * @param node  导出节点的节点路由
     */
    protected fun export(node: Node<BGPRoute>) {

        if (mraiTimer.isRunning) {
            // MRAI 定时器运行时不导出路由
            return
        }

        val selectedRoute = routingTable.getSelectedRoute()

        if (selectedRoute == lastExportedRoute) {
            // 不要连续导出相同的路由
            return
        }

        // 导出当前选择的路线
        node.export(selectedRoute)
        BGPNotifier.notify(ExportNotification(node, selectedRoute))
        lastExportedRoute = selectedRoute

        if (mrai > 0) {
            // 重新启动 MRAI 计时器
            mraiTimer = Timer.enabled(mrai) {
                export(node)    // 当计时器到期时
            }
        }
    }

    /**
     * 重置协议的状态，就像它刚刚创建一样。
     */
    override fun reset() {
        routingTable.clear()
        wasSelectedRouteUpdated = false
        mraiTimer.cancel()
        mraiTimer = Timer.disabled()
        lastExportedRoute = BGPRoute.invalid()
    }

    /**
     * 当协议检测到路由环路时调用。
     */
    abstract protected fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute)

}

/**
 * BGP: 当检测到循环时，它不会做任何额外的事情。
 */
class BGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid())) :
    BaseBGP(mrai, routingTable) {

    override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) = Unit
}
