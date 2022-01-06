package core.routing

/**
 * 抛出异常，表示正在添加的元素已经存在且无法重新添加。
 */
class ElementExistsException(message: String) : Exception(message)

/**
 * 抛出异常，以指示未找到元素并且预期存在元素。
 */
class ElementNotFoundException(message: String) : Exception(message)

/**
 * 用于构建拓扑的构建器。
 *
 * [Topology] 类是不可变的。然而，构建拓扑通常是一个多步骤的过程，因为它需要定义它包含的所有节点以及它们之间的互连。
 * 这个构建器可以做到这一点。它是按照构建器模式实现的。
 *
 * 它提供了通过 ID 添加节点并定义它们之间的连接的方法。最后，它包含一个 [build] 方法，该方法根据传递给构建器的信息构建拓扑。
 *
 */
class TopologyBuilder<R : Route> {

    private val nodes = HashMap<NodeID, Node<R>>()
    private val links = HashSet<Link<R>>()

    /**
     * 添加具有给定 [id] 的新节点并部署给定 [protocol]。如果已经添加了具有给定 [id] 的节点，它会抛出 [ElementExistsException]。
     *
     * @return 此构造器
     */
    @Throws(ElementExistsException::class)
    fun addNode(id: NodeID, protocol: Protocol<R>): TopologyBuilder<R> {

        if (nodes.putIfAbsent(id, Node(id, protocol)) != null) {
            throw ElementExistsException("node with ID `$id` was already added to builder")
        }

        return this
    }

    /**
     * 让构建器从 ID [from] 标识的节点到 ID [to] 标识的节点建立与 [extender] 关联的链接。
     * 该扩展器将用于映射由 [to] 节点导出并在 [from] 节点学习的路由。
     *
     * @return 此构造器
     * @throws ElementNotFoundException 如果 ID 为 [from] 和/或 [to] 的节点尚未添加到构建器中
     * @throws ElementExistsException 如果节点 [from] 和 [to] 之间已经存在链接
     */
    @Throws(ElementExistsException::class, ElementNotFoundException::class)
    fun link(from: NodeID, to: NodeID, extender: Extender<R>): TopologyBuilder<R> {

        val tail = nodes[from] ?: throw ElementNotFoundException("node with ID `$from` was not to builder yet")
        val head = nodes[to] ?: throw ElementNotFoundException("node with ID `$to` was not to builder yet")

        if (!links.add(Link(tail, head, extender))) {
            throw ElementExistsException("nodes $from and $to are already linked")
        }

        head.addInNeighbor(tail, extender)

        return this
    }

    /**
     * 返回一个新的拓扑，其中包含调用此方法时在构建器中定义的节点和链接。
     */
    fun build(): Topology<R> {
        return Topology(nodes)
    }

}