package bgp

import bgp.notifications.BGPNotifier
import bgp.notifications.DetectNotification
import core.routing.Node
import core.routing.RoutingTable
import core.simulator.Time

/**
 * 类似 SS-BGP 的协议的基类。实现邻居的停用并将检测条件留给子类。
 */
abstract class BaseSSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute>): BaseBGP(mrai, routingTable) {

    /**
     * 在检测到路由环路后立即调用 BaseBGP。
     *
     * 类似于 SS-BGP 的协议会检查路由循环是否是循环的，如果是，它会停用发送路由的邻居。
     */
    final override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) {

        // 忽略从禁用邻居那里学到的路由
        if (!routingTable.table.isEnabled(sender)) {
            return
        }

        val prevSelectedRoute = routingTable.getSelectedRoute()

        // 由于检测到环路路由，因此通过发送节点的新路由肯定是无效的

        // 将通过发送者的路由设置为无效这将强制选择器选择替代路由
        val updated = routingTable.update(sender, BGPRoute.invalid())
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        val alternativeRoute = routingTable.getSelectedRoute()
        if (isRecurrent(node, route, alternativeRoute, prevSelectedRoute)) {
            disableNeighbor(sender)
            BGPNotifier.notify(DetectNotification(node, route, alternativeRoute, sender))
        }
    }

    /**
     * 检查检测到的路由循环是否经常发生。子类必须实现这个方法来定义检测条件。
     */
    protected abstract fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute,
                                       alternativeRoute: BGPRoute, prevSelectedRoute: BGPRoute): Boolean

    /**
     * 启用指定的邻居。
     *
     * 可能会更新 `wasSelectedRouteUpdated` 属性。
     *
     * @param neighbor 要启用的邻居
     */
    fun enableNeighbor(neighbor: Node<BGPRoute>) {
        val updated = routingTable.enable(neighbor)
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated
    }

    /**
     * 禁用指定的邻居。
     *
     * 可能会更新 `wasSelectedRouteUpdated` 属性。
     *
     * @param neighbor 要禁用的邻居
     */
    fun disableNeighbor(neighbor: Node<BGPRoute>) {
        val updated = routingTable.disable(neighbor)
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated
    }

    override fun reset() {
        super.reset()
    }
}

/**
 * SS-BGP Protocol: 当检测到循环时，它会尝试使用 WEAK 检测条件检测循环是否重复。如果它确定循环是重复的，它会禁用导出路由的邻居。
 */
class SSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return learnedRoute.localPref > alternativeRoute.localPref
    }
}

/**
 * ISS-BGP:当检测到循环时，它会尝试使用 STRONG 检测条件检测循环是否重复。如果它确定循环是重复的，它会禁用导出路由的邻居。
 */
class ISSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return learnedRoute.localPref > alternativeRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}

/**
 * SS-BGP version 2 Protocol: 它使用比版本 1 更通用的检测条件。
 */
class SSBGP2(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return alternativeRoute.localPref < prevSelectedRoute.localPref
    }
}

/**
 * ISS-BGP version 2 Protocol: 它使用 SS-BGP2 的检测条件，并检查循环路径的尾部是否与替代路由的路径匹配。
 */
class ISSBGP2(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return alternativeRoute.localPref < prevSelectedRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}
