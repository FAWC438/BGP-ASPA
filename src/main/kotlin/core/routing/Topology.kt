package core.routing

/**
 * 拓扑是由节点及其互连组成的网络的高级抽象。
 *
 * 拓扑类是不可变的。也就是说，不能在拓扑中添加或删除节点和链接。必须使用拓扑生成器生成拓扑，@请参阅[TopologyBuilder]。
 *
 * 拓扑中的每个节点都由其ID唯一标识。拓扑通过其[get]方法提供从ID访问其节点的权限。
 *
 * @property size      拓扑中的节点数
 * @property linkCount 拓扑中的链接数
 * @property nodes     包含拓扑中所有节点的集合，无特定顺序
 * @property links     包含拓扑中所有链接的集合，无特定顺序
 *
 */
class Topology<R : Route>(private val idToNode: Map<NodeID, Node<R>>) {

    /**
     * 拓扑中的节点数。
     */
    val size: Int = idToNode.size

    /**
     * 拓扑中的链路数。
     */
    val linkCount: Int
        get() = idToNode.map { it.value.inNeighbors }.count()

    /**
     * 包含拓扑中所有节点的集合，没有特定的顺序。
     */
    val nodes: Collection<Node<R>> = idToNode.values

    /**
     * 包含拓扑中所有链接的集合，没有特定的顺序。
     */
    val links: Collection<Link<R>>
        get() {

            val links = ArrayList<Link<R>>()

            for (node in nodes) {
                for ((neighbor, extender, _) in node.inNeighbors) {
                    links.add(Link(neighbor, node, extender))
                }
            }

            return links
        }

    /**
     * 如果此拓扑不包含任何具有 [id] 的节点，则返回具有 [id] 的节点或 null。
     */
    operator fun get(id: Int): Node<R>? = idToNode[id]

    /**
     * 重置拓扑状态。它重置拓扑中所有节点的状态。
     */
    fun reset() {
        nodes.forEach { it.reset() }
    }

}

/**
 * 表示拓扑中单向链路的数据类。
 *
 * @property tail     链接尾部的节点
 * @property head     链接头部的节点
 * @property extender 用于将头节点导出的路由映射到尾节点的扩展器
 */
data class Link<R : Route>(val tail: Node<R>, val head: Node<R>, val extender: Extender<R>)