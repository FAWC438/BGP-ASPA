package utils

import core.simulator.notifications.*

/**
 * NotificationCollector 收集通知程序发送的所有通知。
 */
open class NotificationCollector : StartListener, EndListener, ThresholdReachedListener,
    MessageSentListener, MessageReceivedListener {

    private val startNotifications = ArrayList<StartNotification>()
    private val endNotifications = ArrayList<EndNotification>()
    private val thresholdReachedNotifications = ArrayList<ThresholdReachedNotification>()
    private val messageSentNotifications = ArrayList<MessageSentNotification>()
    private val messageReceivedNotifications = ArrayList<MessageReceivedNotification>()

    open fun register() {
        Notifier.addStartListener(this)
        Notifier.addEndListener(this)
        Notifier.addThresholdReachedListener(this)
        Notifier.addMessageSentListener(this)
        Notifier.addMessageReceivedListener(this)
    }

    open fun unregister() {
        Notifier.removeStartListener(this)
        Notifier.removeEndListener(this)
        Notifier.removeThresholdReachedListener(this)
        Notifier.removeMessageSentListener(this)
        Notifier.removeMessageReceivedListener(this)
    }

    final override fun onStart(notification: StartNotification) {
        startNotifications.add(notification)
    }

    final override fun onEnd(notification: EndNotification) {
        endNotifications.add(notification)
    }

    final override fun onThresholdReached(notification: ThresholdReachedNotification) {
        thresholdReachedNotifications.add(notification)
    }

    final override fun onMessageSent(notification: MessageSentNotification) {
        messageSentNotifications.add(notification)
    }

    final override fun onMessageReceived(notification: MessageReceivedNotification) {
        messageReceivedNotifications.add(notification)
    }

}

fun collectBasicNotifications(body: () -> Unit): NotificationCollector {

    val collector = NotificationCollector()
    collector.register()
    body()
    collector.unregister()
    return collector
}