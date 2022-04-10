package bgp

import core.routing.Node
import core.routing.RoutingTable
import core.simulator.Time

class ASPA(
    mrai: Time = 0,
    routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid())
) : BaseBGP(mrai, routingTable) {

    override var nodeType: Int = 4

    // 当检测到循环时，它不会做任何额外的事情。
    override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) = Unit
}