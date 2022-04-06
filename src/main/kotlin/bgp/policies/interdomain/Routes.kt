package bgp.policies.interdomain

import bgp.BGPRoute
import core.routing.Path
import core.routing.emptyPath

/**
 * 该文件包含构建域间路由的方法。
 */


// 每个域间路由的 LOCAL-PREF
const val peerplusLocalPreference: Int = 500000
const val peerstarLocalPreference: Int = 400000
const val customerLocalPreference: Int = 300000
const val peerLocalPreference: Int = 200000
const val providerLocalPreference: Int = 100000


/**
 * Returns a peer+ route.
 */
fun peerplusRoute(siblingHops: Int = 0, asPath: Path = emptyPath()) =
    BGPRoute.with(localPref = peerplusLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a peer* route.
 */
fun peerstarRoute(siblingHops: Int = 0, asPath: Path = emptyPath()) =
    BGPRoute.with(localPref = peerstarLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a customer route.
 */
fun customerRoute(siblingHops: Int = 0, asPath: Path = emptyPath()) =
    BGPRoute.with(localPref = customerLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a peer route.
 */
fun peerRoute(siblingHops: Int = 0, asPath: Path = emptyPath()) =
    BGPRoute.with(localPref = peerLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a provider route.
 */
fun providerRoute(siblingHops: Int = 0, asPath: Path = emptyPath()) =
    BGPRoute.with(localPref = providerLocalPreference - siblingHops, asPath = asPath)
