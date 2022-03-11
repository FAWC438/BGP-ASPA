package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * [TimerExpiredEvent] 用于实现定时器。它在计时器启动时发出，并在计时器到期时发生（处理）。它触发 [timer] 的 [Timer.onExpired] 方法。
 */
class TimerExpiredEvent(private val timer: Timer) : Event {

    override fun processIt() {
        timer.onExpired()
    }

}