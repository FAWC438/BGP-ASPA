package core.simulator

import java.util.*

// 时间由一个整数值表示。
typealias Time = Int

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * [Scheduler] 是事件驱动模拟器中最重要的组件。它负责存储模拟过程中发生的所有事件，并根据它们的调度时间交付它们。
 */
class Scheduler {

    /**
     * 预定事件将时间戳与事件相关联。根据此时间戳安排事件。时间戳较低的事件先于时间戳较高的事件。
     */
    private class ScheduledEvent(val time: Time, val event: Event) : Comparable<ScheduledEvent> {
        override operator fun compareTo(other: ScheduledEvent): Int = time.compareTo(other.time)
    }

    /**
     * 优先队列，使所有计划的事件根据其时间戳进行排序。
     */
    private val events = PriorityQueue<ScheduledEvent>()

    /**
     * 跟踪当前时间。调度器的时间对应于从调度器获取的最后一个事件的时间。在从此调度程序中获取任何事件之前，时间为 0。
     */
    var time: Time = 0
        private set

    /**
     * 安排 [event] 在 [timestamp] 给定的某个时间发生。
     *
     * @throws IllegalArgumentException 如果 [timestamp] 低于当前 [time]。
     */
    @Throws(IllegalArgumentException::class)
    fun schedule(event: Event, timestamp: Time) {

        if (timestamp < time) {
            throw IllegalArgumentException(
                "scheduling time '$timestamp' is lower than the " +
                        "current time '$time'"
            )
        }

        events.add(ScheduledEvent(timestamp, event))
    }

    /**
     * 安排 [event] 从当前 [time] 发生 [interval] 时间单位。
     */
    fun scheduleFromNow(event: Event, interval: Time) {
        schedule(event, time + interval)
    }

    /**
     * 检查调度程序队列中是否仍有事件。
     */
    fun hasEvents(): Boolean = !events.isEmpty()

    /**
     * 返回队列中的下一个事件。作为副作用，这也可能会更新此调度程序的当前 [time]。
     *
     * @throws NoSuchElementException 如果调度程序队列中没有更多事件。
     */
    @Throws(NoSuchElementException::class)
    fun nextEvent(): Event {

        val scheduledEvent = events.poll() ?: throw NoSuchElementException("no more events in the queue")

        time = scheduledEvent.time
        return scheduledEvent.event
    }

    /**
     * 重置调度程序。所有事件都从队列中删除，时间设置回 0。
     */
    fun reset() {
        events.clear()
        time = 0
    }

}