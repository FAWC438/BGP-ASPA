package core.routing

import core.simulator.Advertiser

/**
 * 用于节点 ID 的别名
 * 如果认为 Int 太小，可以将 ID 类型更改为 long
 */
typealias NodeID = Int

/**
 * 节点是拓扑的基本元素。
 *
 * 一个节点可以代表任何能够通过公共路由协议与其他节点通话的实体。部署在每个节点的协议不需要完全相同。唯一的要求是交换的路由类型相同。
 *
 * 每个节点都有唯一的 ID。此 ID 仅对于同一拓扑中的节点是唯一的。
 *
 * @property id       节点的 ID，在拓扑中唯一标识它
 * @property protocol 此节点部署的协议
 *
 */
class Node<R : Route>(override val id: NodeID, val protocol: Protocol<R>) : Advertiser<R> {

    /**
     * 包含此节点的邻居的集合。
     */
    private val mutableInNeighbors = ArrayList<Neighbor<R>>()
    val inNeighbors: Collection<Neighbor<R>>
        get() = mutableInNeighbors

    /**
     * 向这个节点添加一个新的邻居。
     *
     * @param neighbor 要添加的邻居节点
     * @param extender 用于将路由从该节点映射到相邻节点的扩展器
     */
    fun addInNeighbor(neighbor: Node<R>, extender: Extender<R>) {
        mutableInNeighbors.add(Neighbor(neighbor, extender))
    }

    /**
     * 让该节点将 [defaultRoute] 设置为其默认路由，并根据其部署的协议规范将其通告给邻居。
     */
    override fun advertise(defaultRoute: R) {
        protocol.setLocalRoute(this, defaultRoute)
    }

    /**
     * 让这个节点向它的所有邻居发送一条包含给定 [route] 的消息。
     */
    fun export(route: R) {
        inNeighbors.forEach { send(route, it) }
    }

    /**
     * 让这个节点将包含给定 [route] 的消息发送到 [neighbor]。
     */
    private fun send(route: R, neighbor: Neighbor<R>) {
        val message = Message(this, neighbor.node, neighbor.extender.extend(route, this))

        neighbor.connection.send(message)
    }

    /**
     * 让该节点从外邻居接收 [message]。 [message]通过本节点部署的路由协议进行处理。
     *
     * 当模拟器想要消息到达某个节点时，它应该调用此方法。
     */
    fun receive(message: Message<R>) {
        protocol.process(message)
    }

    /**
     * 重置此节点的状态。
     */
    override fun reset() {
        protocol.reset()
        inNeighbors.forEach { it.connection.reset() }
    }

    /**
     * 如果两个节点具有完全相同的 ID，则认为它们相等。
     *
     * 节点的子类不应覆盖此方法。
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node<*>

        if (id != other.id) return false

        return true
    }

    /**
     * 遵循 equals/hashCode 规则。
     */
    override fun hashCode(): Int = id

    override fun toString(): String = "Node($id)"
}