package core.simulator

import core.routing.NodeID
import core.routing.Route

/**
 *
 * [Advertiser] 是一些可以宣传目的地的实体。此处的术语广告用于指代一个实体，该实体为某个目的地发起和新路由并传播到其邻居。
 *
 * @property id 在某个范围内唯一标识广告商的编号。
 */
interface Advertiser<in R: Route> {

    val id: NodeID

    /**
     * 此广告商是否为某个目的地发起 [defaultRoute] 并使用此广告商实施的任何机制通过网络传播此路由。
     */
    fun advertise(defaultRoute: R)

    // TODO @refactor - remove this method, see RepetitionRunner
    /**
     * 重置广告商的状态。这可能需要在广告之前进行。
     */
    fun reset()
}