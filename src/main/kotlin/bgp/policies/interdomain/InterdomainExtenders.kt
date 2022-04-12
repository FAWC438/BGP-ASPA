package bgp.policies.interdomain

import bgp.BGPRoute
import core.routing.Extender
import core.routing.Node


object CustomerExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        val localPref = when (sender.protocol.nodeType) {
            2, 3 -> customerLocalPreference
            else -> route.localPref
        }

        return when {
            localPref <= peerLocalPreference -> BGPRoute.invalid()
            else -> customerRoute(asPath = route.asPath.append(sender))
        }
    }

    override fun toString(): String {
        return "c"
    }
}

object PeerExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        val localPref = when (sender.protocol.nodeType) {
            1, 3 -> customerLocalPreference
            else -> route.localPref
        }

        return when {
            localPref <= peerLocalPreference -> BGPRoute.invalid()
            else -> peerRoute(asPath = route.asPath.append(sender))
        }
    }

    override fun toString(): String {
        return "r"
    }
}

object ProviderExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

//        val localPref =
//            if (sender.protocol.attackType == 3 || sender.protocol.attackType == 1) customerLocalPreference else route.localPref
//
//        return when {
//            //  !route.isValid() -> BGPRoute.invalid()
//            localPref <= peerLocalPreference -> BGPRoute.invalid()
//            else -> providerRoute(asPath = route.asPath.append(sender, "p"))

        // 所有情况下，都需要把路由导出给客户（通过供应商通道）
        return providerRoute(asPath = route.asPath.append(sender))

    }

    override fun toString(): String {
        return "p"
    }

}

// ASPA模拟中暂不支持此关系
object PeerplusExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> peerplusRoute(asPath = route.asPath.append(sender))
        }
    }

}

// ASPA模拟中暂不支持此关系
object PeerstarExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> peerstarRoute(asPath = route.asPath.append(sender))
        }
    }

}

// ASPA模拟中暂不支持此关系
object SiblingExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            !route.isValid() -> BGPRoute.invalid()
            route === BGPRoute.self() -> customerRoute(siblingHops = 1, asPath = route.asPath.append(sender))
            else -> BGPRoute.with(
                localPref = route.localPref - 1,
                asPath = route.asPath.append(sender)
            )
        }
    }

}