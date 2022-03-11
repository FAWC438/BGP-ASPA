package simulation

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Time


interface Execution<R : Route> {

    /**
     * 使用指定的拓扑和单个广告执行单个模拟执行。
     */
    fun execute(topology: Topology<R>, advertisement: Advertisement<R>, threshold: Time)

    /**
     * 使用具有多个广告的指定拓扑执行单个模拟执行。
     */
    fun execute(topology: Topology<R>, advertisements: List<Advertisement<R>>, threshold: Time)

}