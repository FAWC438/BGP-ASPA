package bgp.notifications

import bgp.BGPRoute
import core.routing.Node
import core.simulator.notifications.Notification

/**
 * 当一个 [node] 有一个 [route] 发送给它的邻居时发出通知。
 *
 * 导出路由不同于发送消息。正在导出的路由表明 [node] 有一个新的 [route] 可以发送给它的邻居。
 * 这可能导致发送零个或多个消息：在一次导出期间，节点向每个相邻节点发送一条消息。
 *
 */
data class ExportNotification(
        val node: Node<BGPRoute>,
        val route: BGPRoute
) : Notification()

/**
 * 当 [node] 从 [neighbor] 学习 [route] 时发出通知。
 *
 * 节点接收到的路由与发送方选择的路由完全对应。但是，在实际网络中并非如此。
 * 在真实网络中，节点从邻居那里学到的路由由发送方的导出策略和接收方的导入策略给出。
 * 模拟器使用扩展函数来模拟该转换，@see [core.routing.Extender]。
 * 因此，学习到的 [route] 对应于将与从 [neighbor] 到 [node] 的链路关联的扩展器应用于在 [node] 处接收到的路由的结果。
 * 一个例外是，当生成的路由已经在其路径中包含 [node] 时。在这种情况下，学习到的路由是无效路由。
 *
 */
data class LearnNotification(
        val node: Node<BGPRoute>,
        val route: BGPRoute,
        val neighbor: Node<BGPRoute>
) : Notification()

/**
 * Notification issued when a [node] detects a recurrent routing loop after processing a route
 * from a [neighbor].
 *
 * This notification only applies to SS-BGP protocols.
 *
 * @property learnedRoute  the route learned from [neighbor]
 * @property selectedRoute the route selected by [node] as a result o learning the [learnedRoute]
 * from [neighbor]
 *
 */
data class DetectNotification(
        val node: Node<BGPRoute>,
        val learnedRoute: BGPRoute,
        val selectedRoute: BGPRoute,
        val neighbor: Node<BGPRoute>
) : Notification()

/**
 * 当 [node] 在路由器 [previous Route] 上选择新路由 [selected Route] 时发出通知。
 *
 */
data class SelectNotification(
        val node: Node<BGPRoute>,
        val selectedRoute: BGPRoute,
        val previousRoute: BGPRoute
) : Notification()


