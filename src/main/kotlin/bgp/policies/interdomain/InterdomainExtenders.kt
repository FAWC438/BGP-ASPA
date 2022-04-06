package bgp.policies.interdomain

import bgp.BGPRoute
import core.routing.Extender
import core.routing.Node


object CustomerExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            // peer 或者 provider 将不能够通过 customer 关系链路进行路由
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> customerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> peerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object ProviderExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            //  !route.isValid() -> BGPRoute.invalid()
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> providerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerplusExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> peerplusRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerstarExtender : Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= peerLocalPreference || route.localPref == peerstarLocalPreference -> BGPRoute.invalid()
            else -> peerstarRoute(asPath = route.asPath.append(sender))
        }
    }

}

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