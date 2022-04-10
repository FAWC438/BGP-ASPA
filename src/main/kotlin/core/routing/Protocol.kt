package core.routing

/**
任何协议实现的基本接口。所有的协议实现都必须实现这个接口。
 *
 * @property selectedRoute 此协议选择的路由
 *
 */
interface Protocol<R : Route> {

    /**
     * 协议选择的路由。该路线可能会在模拟执行期间发生变化。
     */
    val selectedRoute: R

    /**
     * 判断是否为路由泄露攻击者
     *
     * 攻击者有不同的类型
     *
     * - 类型0 - 不是攻击者
     * - 类型1 - 将 peer 的路由泄露给其他的 peer/provider，即本地优先级变为 customer
     * - 类型2 - 将 provider 的路由泄露给其他的 peer/provider，即本地优先级变为 customer
     * - 类型3 - 包含以上两种情况的攻击者节点
     *
     */
    var attackType: Int

    /**
     * 为 [node] 设置本地 [route]。
     */
    fun setLocalRoute(node: Node<R>, route: R)

    /**
     * 有协议进程和传入的[message]。
     *
     * 接收节点在收到消息时调用此方法。
     */
    fun process(message: Message<R>)

    /**
     * 将协议的状态重置为其初始状态。
     */
    fun reset()

}