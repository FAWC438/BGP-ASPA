package bgp

import core.routing.Path
import core.routing.Route
import core.routing.emptyPath

const val PRLocalPreference: Int = Int.MIN_VALUE + 1
const val PCLocalPreference: Int = Int.MIN_VALUE + 2
const val RRLocalPreference: Int = Int.MIN_VALUE + 3
const val RCLocalPreference: Int = Int.MIN_VALUE + 4

/**
 *
 * 一条 BGP 路由由两个属性组成：LOCAL-PREF 和 AS-PATH。
 * LOCAL-PREF 由每个节点在本地分配，并指示该节点分配给每个路由的优先级。
 * AS-PATH 包含路由从原始广告者到持有该路由的当前节点所经过的节点序列。
 *
 * BGPRoute始终是不可变的实例！
 *
 */
sealed class BGPRoute : Route {

    abstract val localPref: Int
    abstract val asPath: Path

    companion object Factory {

        /**
         * 返回具有指定 LOCAL-PREF 和 AS-PATH 的 BGP 路由。
         */
        fun with(localPref: Int, asPath: Path): BGPRoute = ValidBGPRoute(localPref, asPath)

        /**
         * 返回无效的 BGP 路由。
         */
        fun invalid(): BGPRoute = InvalidBGPRoute

        /**
         * 返回收到路由泄露的 BGP 路由。
         */
        fun leakingRoute(leakingType: Int, ASPath: Path): BGPRoute {
            return when (leakingType) {
                0 -> LeakingBGPRoutePR(PRLocalPreference, ASPath)
                1 -> LeakingBGPRoutePC(PCLocalPreference, ASPath)
                2 -> LeakingBGPRouteRR(RRLocalPreference, ASPath)
                3 -> LeakingBGPRouteRC(RCLocalPreference, ASPath)
                else -> throw Exception("Unknown leaking type")
            }
        }

        /**
         * 返回自 BGP 路由。self BGP 路由是具有最高优先级的 BGP 路由。
         */
        fun self(): BGPRoute = SelfBGPRoute

    }

    /**
     * 有效 BGP 路由的实现。
     */
    private data class ValidBGPRoute(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = true
    }

    /**
     * 无效 BGP 路由的实现。
     */
    private object InvalidBGPRoute : BGPRoute() {
        override val localPref: Int = Int.MIN_VALUE
        override val asPath: Path = emptyPath()
        override fun isValid(): Boolean = false
        override fun toString(): String = "•"
    }

    /**
     * 遭到路由泄露攻击的BGP路由实现，p - r 型
     */
    private data class LeakingBGPRoutePR(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = false
        override fun toString(): String = "*-pr"
    }

    /**
     * 遭到路由泄露攻击的BGP路由实现，p - c 型
     */
    private data class LeakingBGPRoutePC(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = false
        override fun toString(): String = "*-pc"
    }

    /**
     * 遭到路由泄露攻击的BGP路由实现，r - r 型
     */
    private data class LeakingBGPRouteRR(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = false
        override fun toString(): String = "*-rr"
    }

    /**
     * 遭到路由泄露攻击的BGP路由实现，r - c 型
     */
    private data class LeakingBGPRouteRC(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = false
        override fun toString(): String = "*-rc"
    }

    /**
     * self BGP 路由的实现。self BGP 路由是具有最高优先级的 BGP 路由。
     */
    private object SelfBGPRoute : BGPRoute() {
        override val localPref: Int = Int.MAX_VALUE
        override val asPath: Path = emptyPath()
        override fun isValid(): Boolean = true
        override fun toString(): String = "◦"
    }

    override fun toString(): String {
        return "BGPRoute(localPref=$localPref, asPath=$asPath)"
    }

    /**
     * 当且仅当两条 BGP 路由具有完全相同的本地优先级值和相同的 AS 路径时，它们才被认为是相等的
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BGPRoute

        if (localPref != other.localPref) return false

        // 避免大多数时候必须比较 AS 路径的每个节点的技巧
        if (asPath.size != other.asPath.size) return false

        if (asPath != other.asPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = localPref
        result = 31 * result + asPath.hashCode()
        return result
    }


}

/**
 * BGP 路由的比较功能。它比较两条 BGP 路由的优先级。
 *
 * BGP路由的优先级由以下属性决定：
 *
 *  1. the LOCAL-PREF
 *  2. the length of the AS-PATH
 *  3. the ID of the next-hop node
 *
 * @return 如果 route1 优于 route 2，则为正值；如果他们有相同的偏好，则为零；如果 route2 优于 route1，则为负值
 */
fun bgpRouteCompare(route1: BGPRoute, route2: BGPRoute): Int {

    var difference = route1.localPref.compareTo(route2.localPref)
    if (difference == 0) {
        difference = route2.asPath.size.compareTo(route1.asPath.size)
        if (difference == 0) {

            val nextHop1 = route1.asPath.nextHop() ?: return 0
            val nextHop2 = route2.asPath.nextHop() ?: return 0

            difference = nextHop2.id.compareTo(nextHop1.id)
        }
    }

    return difference
}
