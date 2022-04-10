package bgp

import core.routing.Node
import core.routing.RoutingTable
import core.simulator.Time

class Attacker(
    mrai: Time = 0,
    routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()),
    aType: Int
) :
    BaseBGP(mrai, routingTable) {

    override var attackType: Int = aType


    // 当检测到循环时，它不会做任何额外的事情。
    override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) = Unit
}