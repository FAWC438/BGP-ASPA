package bgp.notifications


object BGPNotifier {

    //region Lists containing the registered listeners

    private val learnListeners = mutableListOf<LearnListener>()
    private val detectListeners = mutableListOf<DetectListener>()
    private val selectListeners = mutableListOf<SelectListener>()
    private val exportListeners = mutableListOf<ExportListener>()

    //endregion

    //region Learn notification

    /**
     * 告诉通知者 [listener] 想要接收学习通知。之后，[listener] 将收到所有学习通知。
     */
    fun addLearnListener(listener: LearnListener) {
        learnListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收学习通知。之后，[listener] 将不再收到学习通知。
     */
    fun removeLearnListener(listener: LearnListener) {
        learnListeners.remove(listener)
    }

    /**
     * 向所有收听学习通知的听众发送 [notification]。
     */
    fun notify(notification: LearnNotification) {
        learnListeners.forEach { it.onLearn(notification) }
    }

    //endregion

    //region Detect notification

    /**
     * 告诉通知者 [listener] 想要接收检测通知。之后，[listener] 将收到所有检测通知。
     */
    fun addDetectListener(listener: DetectListener) {
        detectListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收检测通知。之后，[listener] 将不再收到检测通知。
     */
    fun removeDetectListener(listener: DetectListener) {
        detectListeners.remove(listener)
    }

    /**
     * 向所有侦听器发送 [notification] 以检测通知。
     */
    fun notify(notification: DetectNotification) {
        detectListeners.forEach { it.onDetect(notification) }
    }

    //endregion

    //region Select notification

    /**
     * 告诉通知者 [listener] 想要接收选择通知。之后，[listener] 将收到所有选择通知。
     */
    fun addSelectListener(listener: SelectListener) {
        selectListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收选择通知。之后，[listener] 将不再收到选择通知。
     */
    fun removeSelectListener(listener: SelectListener) {
        selectListeners.remove(listener)
    }

    /**
     * 向所有侦听选择通知的侦听器发送 [notification]。
     */
    fun notify(notification: SelectNotification) {
        selectListeners.forEach { it.onSelect(notification) }
    }

    //endregion

    //region Export notification

    /**
     * 告诉通知者 [listener] 想要接收导出通知。之后，[listener] 将收到所有导出通知。
     */
    fun addExportListener(listener: ExportListener) {
        exportListeners.add(listener)
    }

    /**
     * 告诉通知者 [listener] 不再想接收导出通知。之后，[listener] 将不再收到导出通知。
     */
    fun removeExportListener(listener: ExportListener) {
        exportListeners.remove(listener)
    }

    /**
     * 向所有收听导出通知的侦听器发送 [notification]。
     */
    fun notify(notification: ExportNotification) {
        exportListeners.forEach { it.onExport(notification) }
    }

    //endregion

}