package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * [Timer] 用于安排在一段时间后执行的操作。计时器对象是一次性使用对象。也就是说，定时器在创建后立即启动，并且只能启动一次。
 * 过期后，那个对象就完全没用了。
 *
 * 有两种 [Timer] 实现：EnabledTimer 和 DisabledTimer。
 * 前者是定时器的实际实现。后者只是一个虚拟实现，表示禁用的计时器，因此永远不会运行。
 *
 * @property isRunning flag indicating whether or not the timer is running
 */
sealed class Timer {

    abstract val isRunning: Boolean

    companion object Factory {

        /**
         * 返回具有指定持续时间和操作的启用计时器。
         */
        fun enabled(duration: Time, action: () -> Unit): Timer = EnabledTimer(duration, action)

        /**
         * 返回一个禁用的计时器。
         */
        fun disabled(): Timer = DisabledTimer

    }

    /**
     * 如果计时器尚未到期，则取消计时器。
     */
    abstract fun cancel()

    /**
     * 应在计时器到期时调用。
     */
    abstract fun onExpired()

    private class EnabledTimer(duration: Time, private val action: () -> Unit) : Timer() {

        override var isRunning: Boolean = true
            private set

        private var isCanceled = false

        init {
            Simulator.scheduler.scheduleFromNow(TimerExpiredEvent(this), duration)
        }

        /**
         * 取消定时器。如果在定时器超时之前调用，那么当定时器超时时定时器的动作不会被执行。调用 [cancel] 后，计时器停止运行。
         */
        override fun cancel() {
            isCanceled = true
            isRunning = false
        }

        /**
         * 当计时器到期执行 [action] 时调用。如果计时器未取消，则执行 [action]。
         */
        override fun onExpired() {
            if (!isCanceled) {
                isRunning = false
                action()
            }
        }

    }

    private object DisabledTimer : Timer() {

        // 禁用的计时器永远不会运行
        override val isRunning: Boolean = false

        override fun cancel() = Unit

        override fun onExpired() = Unit

    }

}