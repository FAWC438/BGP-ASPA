package core.routing

import core.simulator.Connection

/**
 * 邻居是一个[node]，本地节点通过[connection]向其发送消息。
 *
 * 发送到邻居的路由通过与该邻居关联的 [extender] 进行扩展。
 *
 * @property node       该相邻节点的引用
 * @property extender   将本地节点导出的路由映射到相邻节点的扩展器
 * @property connection 与邻居[node]的连接，用于向邻居发送消息
 *
 */
data class Neighbor<R : Route>(
    val node: Node<R>,
    val extender: Extender<R>,
    val connection: Connection<R> = Connection()
)