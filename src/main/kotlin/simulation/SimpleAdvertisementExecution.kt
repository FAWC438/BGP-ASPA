package simulation

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Simulator
import core.simulator.Time
import java.io.IOException


class SimpleAdvertisementExecution<R : Route> : Execution<R> {

    val dataCollectors = DataCollectorGroup()

    /**
     * 执行模拟，从中收集数据并报告结果。
     *
     * 要收集数据，在调用此方法之前，必须指定要使用的数据收集器，方法是将每个收集器添加到此执行的数据收集器组中。
     *
     * @throws IOException 如果发生 IO 错误
     */
    @Throws(IOException::class)
    override fun execute(topology: Topology<R>, advertisement: Advertisement<R>, threshold: Time) {
        execute(topology, listOf(advertisement), threshold)
    }

    /**
     * 执行模拟，从中收集数据并报告结果。
     *
     * 要收集数据，在调用此方法之前，必须指定要使用的数据收集器，方法是将每个收集器添加到此执行的数据收集器组中。
     *
     * @throws IOException 如果发生 IO 错误
     */
    @Throws(IOException::class)
    override fun execute(
        topology: Topology<R>, advertisements: List<Advertisement<R>>,
        threshold: Time
    ) {

        dataCollectors.clear()

        // 在此处运行模拟器
        val data = dataCollectors.collect {
            Simulator.simulate(topology, advertisements, threshold)
        }

        data.report()
    }
}