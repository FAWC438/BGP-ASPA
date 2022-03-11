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
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class ExportNotification(
        val node: Node<BGPRoute>,
        val route: BGPRoute
) : Notification()

/**
 * 当 [node] 从 [neighbor] 学习 [route] 时发出通知。
 *
 * The route a node receives corresponds exactly to the route selected at the sender. However, in
 * a real network that is not the case. In a real network the route learned at a node from a
 * neighbor is given by the export policies of the sender and the import policies of the receiver.
 * The simulator uses an extending function to model that transformation, @see
 * [core.routing.Extender]. Thus, the learned [route] corresponds to the result of applying the
 * extender associated with the link from [neighbor] to [node] to route received at [node]. One
 * exception to this, is when the resulting route already include [node] in its path. In that
 * case, the learned route is the invalid route.
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
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
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class DetectNotification(
        val node: Node<BGPRoute>,
        val learnedRoute: BGPRoute,
        val selectedRoute: BGPRoute,
        val neighbor: Node<BGPRoute>
) : Notification()

/**
 * Notification issued when a [node] selects a new route [selectedRoute] over route [previousRoute].
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class SelectNotification(
        val node: Node<BGPRoute>,
        val selectedRoute: BGPRoute,
        val previousRoute: BGPRoute
) : Notification()


