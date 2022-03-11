package core.simulator

import core.routing.Route
import core.routing.Topology
import core.simulator.notifications.Notifier
import core.simulator.notifications.EndNotification
import core.simulator.notifications.StartNotification
import core.simulator.notifications.ThresholdReachedNotification
import java.util.*

/**
 *
 * [Simulator] 是运行模拟的起点。它提供了 [simulate] 方法，这些方法是开始运行模拟的受支持方式。
 * 他们根据给定的参数设置模拟，然后运行模拟循环，该循环或多或少包括从 [scheduler] 获取预定事件并处理它们。
 *
 * [Simulator] 还包含用于模拟的 [scheduler] 和 [messageDelayGenerator]。
 * 使用 [Simulator] 指定的 [scheduler] 调度模拟事件。消息延迟也是如此：这些是由 [messageDelayGenerator] 生成的。
 *
 */
object Simulator {

    /**
     * 保存模拟中使用的调度程序。
     */
    var scheduler = Scheduler()

    /**
     * 用于生成消息延迟的生成器。默认情况下，它使用 [NoDelayGenerator]，考虑 [RandomDelayGenerator] 作为模拟不同行为的替代方案。
     */
    var messageDelayGenerator: DelayGenerator = NoDelayGenerator

    /**
     * 将模拟器的配置重置为其默认值。
     */
    fun resetToDefaults() {
        scheduler = Scheduler()
        messageDelayGenerator = NoDelayGenerator
    }

    /**
     * 在 [topology] 上运行具有多个 [advertisements] 的模拟。通过安排[advertisements]来启动模拟。
     * 之后，运行一个事件循环，从 [scheduler] 获取每个事件并对其进行处理。
     * 一旦[scheduler]中没有更多的模拟事件或达到[threshold]，模拟就会结束。
     * 如果未指定 [threshold] 值，则模拟将运行，直到调度程序没有更多事件要处理。
     *
     * 在模拟期间，通知器（@see [Notifier]）可能会发送一些通知，这些通知提供有关路由事件和模拟进度的信息。
     *
     * Warning: 在运行模拟之前，调度程序应当被重置！！
     *
     * @return 如果模拟在达到 [threshold] 之前结束，则为 true，否则为 false。
     * @throws IllegalArgumentException 如果 [advertisements] 包含0个通告
     */
    @Throws(IllegalArgumentException::class)
    fun <R : Route> simulate(
        topology: Topology<*>, advertisements: List<Advertisement<R>>,
        threshold: Time = Int.MAX_VALUE
    ): Boolean {

        if (advertisements.isEmpty()) {
            throw IllegalArgumentException("a simulation requires at least one advertisement")
        }

        // 在开始模拟之前确保调度程序是完全干净的
        scheduler.reset()

        Notifier.notify(StartNotification(messageDelayGenerator.seed, topology))

        // 安排策略中指定的通告
        for (advertisement in advertisements) {
            scheduler.schedule(advertisement)
        }

        // 指示模拟是否在达到阈值之前完成的标志
        var terminatedBeforeThreshold = true

        while (scheduler.hasEvents()) {
            val event = scheduler.nextEvent()

            // 检查是否达到阈值：
            // 此验证需要在获取下一个事件后执行，因为执行该操作时会更新调度程序的时间
            if (currentTime() >= threshold) {
                Notifier.notify(ThresholdReachedNotification(threshold))
                terminatedBeforeThreshold = false
                break
            }

            event.processIt()
        }

        // 通知监听器模拟结束
        Notifier.notify(EndNotification(topology))

        return terminatedBeforeThreshold
    }

    /**
     * 在 [topology] 上运行具有多个 [advertisement] 的模拟。 通过安排[advertisement]来启动模拟。
     * 之后，运行一个事件循环，从 [scheduler] 获取每个事件并对其进行处理。
     * 一旦[scheduler]中没有更多的模拟事件或达到[threshold]，模拟就会结束。
     * 如果未指定 [threshold] 值，则模拟将运行，直到调度程序没有更多事件要处理。
     *
     * 在模拟期间，通知器（@see [Notifier]）可能会发送一些通知，这些通知提供有关路由事件和模拟进度的信息。
     *
     * Warning: 在运行模拟之前，调度程序被重置！！
     *
     * @return 如果模拟在达到 [threshold] 之前结束，则为 true，否则为 false。
     */
    fun <R : Route> simulate(
        topology: Topology<R>, advertisement: Advertisement<R>,
        threshold: Time = Int.MAX_VALUE
    ): Boolean {
        return simulate(topology, listOf(advertisement), threshold)
    }

    /**
     * 返回模拟器版本。
     * 它从定义模拟器版本的资源中获取版本。
     */
    fun version(): String {

        javaClass.getResourceAsStream("/version.properties").use {
            val properties = Properties()
            properties.load(it)
            return properties.getProperty("application.version")
        }
    }

}

/**
 * 安排广播事件。
 */
private fun <R : Route> Scheduler.schedule(advertisement: Advertisement<R>) {
    schedule(AdvertiseEvent(advertisement.advertiser, advertisement.route), advertisement.time)
}

/**
 * 访问模拟时间的更简洁的方式。
 */
@Suppress("NOTHING_TO_INLINE")
inline fun currentTime(): Time = Simulator.scheduler.time