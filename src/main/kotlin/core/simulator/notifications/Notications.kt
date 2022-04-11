package core.simulator.notifications

import core.routing.Message
import core.routing.Topology
import core.simulator.Time
import core.simulator.currentTime

/**
 *
 * 所有通知的基类。所有通知都与发出通知的 [time] 相关联。
 *
 * @property time 发出通知的时间
 */
abstract class Notification(val time: Time = currentTime())

/**
 *
 * 模拟开始时发出的通知。
 *
 * @property seed     用于生成消息延迟的初始种子
 * @property topology 用于仿真的拓扑
 */
data class StartNotification(val seed: Long, val topology: Topology<*>) : Notification()

/**
 *
 * 模拟结束时发出的通知。无论是否达到模拟的阈值，都会发出它。
 *
 * @property topology 用于仿真的拓扑
 */
data class EndNotification(val topology: Topology<*>) : Notification()

/**
 *
 * Notification issued when the threshold of the simulation is reached.
 *
 * @property threshold the value of the threshold set for the simulation
 */
class ThresholdReachedNotification(val threshold: Time) : Notification()

/**
 *
 * Notification issued when a [message] is sent.
 */
data class MessageSentNotification(val message: Message<*>) : Notification()

/**
 *
 * Notification issued when a [message] arrives at its recipient.
 */
data class MessageReceivedNotification(val message: Message<*>) : Notification()