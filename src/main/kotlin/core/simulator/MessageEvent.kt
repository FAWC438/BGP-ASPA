package core.simulator

import core.routing.Message
import core.routing.Route
import core.simulator.notifications.MessageReceivedNotification
import core.simulator.notifications.Notifier

/**
 *
 * [MessageEvent] 在发送消息时发出，并且在消息传递到其接收节点时发生（此消息此时正在被处理）。
 */
class MessageEvent<R : Route>(private val message: Message<R>) : Event {

    /**
     * 将 [message] 发送到其接收节点。
     */
    override fun processIt() {
        Notifier.notify(MessageReceivedNotification(message))
        message.recipient.receive(message)
    }

}