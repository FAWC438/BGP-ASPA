package core.simulator

import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * [Advertisement] 实例包含描述通告的所有信息：执行它的 [advertiser]；被通告的[route]；以及通告发生的[time]。
 */
data class Advertisement<R : Route>(
    val advertiser: Advertiser<R>,
    val route: R,
    val time: Time = 0
)