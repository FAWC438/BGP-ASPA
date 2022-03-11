package core.simulator

/**
 *
 * 模拟事件的接口。
 */
interface Event {

    /**
     * 处理此事件。在应该处理事件时调用此方法。子类应该使用这个方法来实现这个事件触发的任何动作。
     */
    fun processIt()
}