package core.simulator.notifications

/**
 *
 * 通知侦听器的标记接口。
 *
 * 在模拟期间会发出多个通知。这些通知提供有关正在模拟的路由协议的状态和进度的有价值的信息。
 * 因此，收听这些通知可能有助于收集相关数据。
 *
 * [NotificationListener] 侦听特定通知。要监听多个通知，子类应该实现多种类型的监听器。
 */
interface NotificationListener

/**
 *
 * [StartListener] 侦听 [StartNotification]。
 */
interface StartListener : NotificationListener {

    /**
     * 在发出开始通知时调用。
     */
    fun onStart(notification: StartNotification)
}

/**
 *
 * [EndListener] 侦听 [EndNotification]。
 */
interface EndListener {

    /**
     * 在发出结束通知时调用。
     */
    fun onEnd(notification: EndNotification)
}

/**
 *
 * [ThresholdReachedListener] 侦听 [ThresholdReachedNotification]。
 */
interface ThresholdReachedListener {

    /**
     * 在发出达到阈值的通知时调用。
     */
    fun onThresholdReached(notification: ThresholdReachedNotification)
}

/**
 *
 * [MessageSentListener] 侦听 [MessageSentNotification]。
 */
interface MessageSentListener : NotificationListener {

    /**
     * 在发出消息已发送通知时调用。
     */
    fun onMessageSent(notification: MessageSentNotification)
}

/**
 *
 * [MessageReceivedListener] 侦听 [MessageReceivedNotification]。
 */
interface MessageReceivedListener : NotificationListener {

    /**
     * 在发出消息接收通知时调用。
     */
    fun onMessageReceived(notification: MessageReceivedNotification)
}