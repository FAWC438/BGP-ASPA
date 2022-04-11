package bgp.notifications

/**
 * [ExportListener] 侦听 [ExportNotification]。
 *
 */
interface ExportListener {

    /**
     * 发出出口通知时调用。
     */
    fun onExport(notification: ExportNotification)
}

/**
 * [LearnListener] 监听 [LearnNotification]。
 *
 */
interface LearnListener {

    /**
     * 在发出学习通知时调用。
     */
    fun onLearn(notification: LearnNotification)
}

/**
 * [DetectListener] 侦听 [DetectNotification]。
 *
 */
interface DetectListener {

    /**
     * 发出检测通知时调用。
     */
    fun onDetect(notification: DetectNotification)
}

/**
 * [SelectListener] 监听 [SelectNotification]。
 *
 */
interface SelectListener {

    /**
     * 在发出选择通知时调用。
     */
    fun onSelect(notification: SelectNotification)
}
