package core.simulator

import core.routing.Message
import core.routing.Route
import core.simulator.notifications.MessageSentNotification
import core.simulator.notifications.Notifier

/**
 *
 * 此类抽象了消息可以通过的连接。一个连接可以在一个方向上传送消息。
 *
 * [Connection] 类提供了一个 [send] 方法，该方法抽象了通过连接发送消息的过程。
 * 通过连接发送的消息会受到从延迟生成器 [Simulator.messageDelayGenerator] 获得的随机延迟的影响。
 * 尽管此生成器会产生延迟，但路由消息始终以先进先出的顺序传递。
 *
 */
class Connection<R : Route> {

    /**
     * 跟踪通过此连接发送的最后一条消息的传递时间。
     */
    private var lastDeliverTime = 0

    /**
     * 通过此连接发送 [message]。它使消息受到从延迟生成器 [Simulator.messageDelayGenerator] 获得的随机延迟的影响。
     *
     * !! 它将事件添加到调度程序 [Simulator.scheduler] !!
     */
    fun send(message: Message<R>): Time {

        val delay = Simulator.messageDelayGenerator.nextDelay()
        val deliverTime = maxOf(Simulator.scheduler.time + delay, lastDeliverTime) + 1

        Simulator.scheduler.schedule(MessageEvent(message), deliverTime)
        lastDeliverTime = deliverTime

        Notifier.notify(MessageSentNotification(message))
        return deliverTime
    }

    /**
     * 重置连接
     *
     * 重置后，连接将不会记住它可能发送的先前消息。因此，通过此连接发送的新消息可能会在调用 [reset] 之前发送的消息之前传递。
     * 为避免意外行为，请仅在确保所有发送的消息都已送达后调用此方法。
     *
     */
    fun reset() {
        lastDeliverTime = 0
    }

}