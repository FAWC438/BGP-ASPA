package core.simulator

import core.routing.Route

/**
 *
 * 通告事件触发 [advertiser] 的 [Advertiser.advertise] 方法使其广播 [route]。
 */
class AdvertiseEvent<R : Route>(private val advertiser: Advertiser<R>, private val route: R) : Event {

    override fun processIt() {
        advertiser.advertise(route)
    }
}