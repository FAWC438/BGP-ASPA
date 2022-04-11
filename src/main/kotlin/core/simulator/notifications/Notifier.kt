package core.simulator.notifications

/**
 *
 * [Notifier] 负责让每个通知到达每个侦听该类型通知的侦听器。
 * [Notifier] 仅支持此包中包含的通知。监听者需要注册到 [Notifier] 才能接收通知。
 *
 * 它为每种类型的通知提供了三种方法：
 *
 *   - add:    由侦听器调用接收特定类型的通知
 *   - remove: 由侦听器调用以停止接收特定类型的通知
 *   - notify: 在模拟过程中由模拟器调用以向侦听器发送通知
 *
 */
object Notifier {

    //region 包含已注册侦听器的列表

    private val startListeners = mutableListOf<StartListener>()
    private val endListeners = mutableListOf<EndListener>()
    private val thresholdReachedListeners = mutableListOf<ThresholdReachedListener>()
    private val messageSentListeners = mutableListOf<MessageSentListener>()
    private val messageReceivedListeners = mutableListOf<MessageReceivedListener>()

    //endregion

    //region 开始通知

    /**
     * 告诉通知者 [listener] 想要接收开始通知。之后，[listener] 将收到所有开始通知。
     */
    fun addStartListener(listener: StartListener) {
        startListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收开始通知。之后，[listener] 将不再收到开始通知。
     */
    fun removeStartListener(listener: StartListener) {
        startListeners.remove(listener)
    }

    /**
     * 向所有侦听开始通知的侦听器发送 [notification]。
     */
    fun notify(notification: StartNotification) {
        startListeners.forEach { it.onStart(notification) }
    }

    //endregion

    //region 结束通知

    /**
     * 告诉通知者 [listener] 想要接收结束通知。之后，[listener] 将收到所有结束通知。
     */
    fun addEndListener(listener: EndListener) {
        endListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收结束通知。之后，[listener] 将不再收到结束通知。
     */
    fun removeEndListener(listener: EndListener) {
        endListeners.remove(listener)
    }

    /**
     * 向所有收听结束通知的侦听器发送 [notification]。
     */
    fun notify(notification: EndNotification) {
        endListeners.forEach { it.onEnd(notification) }
    }

    //endregion

    //region 达到阈值通知

    /**
     * 告诉通知者 [listener] 想要接收达到阈值的通知。之后，[listener] 将收到所有达到阈值的通知。
     */
    fun addThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再希望接收已达到阈值的通知。之后，[listener] 将不再收到达到阈值的通知。
     */
    fun removeThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.remove(listener)
    }

    /**
     * 向所有侦听阈值已达到通知的侦听器发送 [notification]。
     */
    fun notify(notification: ThresholdReachedNotification) {
        thresholdReachedListeners.forEach { it.onThresholdReached(notification) }
    }

    //endregion

    //region 消息发送通知

    /**
     * 告诉通知者 [listener] 想要接收消息发送的通知。之后，[listener] 将收到所有消息发送的通知。
     */
    fun addMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收消息发送的通知。之后，[listener] 将不再接收消息发送通知。
     */
    fun removeMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.remove(listener)
    }

    /**
     * 向所有收听消息发送通知的侦听器发送 [通知]。
     */
    fun notify(notification: MessageSentNotification) {
        messageSentListeners.forEach { it.onMessageSent(notification) }
    }

    //endregion

    //region 消息收到通知

    /**
     * 注册一个新的消息接收侦听器。
     *
     * @param listener 消息接收到的监听器注册。
     */
    fun addMessageReceivedListener(listener: MessageReceivedListener) {
        messageReceivedListeners.add(listener)
    }

    /**
     * 取消注册接收到的新消息侦听器。
     *
     * @param listener 消息接收到的侦听器取消注册。
     */
    fun removeMessageReceivedListener(listener: MessageReceivedListener) {
        messageReceivedListeners.remove(listener)
    }

    /**
     * 向每个收到消息的侦听器发送收到消息的通知。
     *
     * @param notification 消息收到通知发送给每个注册的侦听器。
     */
    fun notify(notification: MessageReceivedNotification) {
        messageReceivedListeners.forEach { it.onMessageReceived(notification) }
    }

    //endregion

}