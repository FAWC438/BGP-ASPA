package core.routing

/**
 * 路径是在网络中形成路径的节点序列。
 *
 * 附加到路径的节点保持与添加时相同的顺序。
 *
 * 路径实例是不可变的！所有会修改路径的操作（@see [Path.append]）实际上并不修改该路径实例。
 * 相反，它们会生成一个带有相应修改的新实例并返回该实例。
 *
 * 同一节点可以多次添加到路径中。如果路由协议不允许这样做，协议会负责确保不会发生这种情况。
 *
 * 请注意，尽管 Path 类包括节点，但它不需要指定路由类型。那是故意的。
 * path 类只存储节点，它只关心它们的顺序。它不执行任何需要知道路由类型的操作。
 *
 * @property size 路径中的节点数
 *
 */
class Path internal constructor(private val nodes: List<Node<*>>) : Iterable<Node<*>> {

    val size: Int = nodes.size

    /**
     * 返回一个新的路径实例，其中 [node] 添加到（附加到）此路径的末尾。
     */
    fun append(node: Node<*>): Path {
        val nodesCopy = ArrayList(nodes)
        nodesCopy.add(node)

        return Path(nodesCopy)
    }

    /**
     * 返回路径的下一跳节点。那是该路径末端的节点。如果路径为空，则返回 null。
     */
    fun nextHop(): Node<*>? {
        return nodes.lastOrNull()
    }

    /**
     * 检查此路径是否包含 [node]。
     */
    operator fun contains(node: Node<*>) = node in nodes

    /**
     * 返回此路径的浅表副本。换句话说，以完全相同的顺序返回包含完全相同节点的路径实例。
     */
    fun copy(): Path = Path(nodes)  // 这仅作为副本起作用，因为路径是不可变的

    /**
     * 返回从路径开始到第一个节点等于 [node] 的子路径对应的路径。
     */
    fun subPathBefore(node: Node<*>): Path {
        val nodeIndex = nodes.indexOf(node)
        return if (nodeIndex >= 0) Path(nodes.subList(0, nodeIndex)) else this
    }

    /**
     * 返回路径节点上的迭代器。该迭代器通过从第一个节点开始的路径。
     */
    override fun iterator(): Iterator<Node<*>> {
        return nodes.iterator()
    }

    /**
     * 如果两条路径以完全相同的顺序具有完全相同的节点，则它们被认为是相等的。
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Path

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return "$nodes"
    }

}

/**
 * 返回没有节点的路径。
 */
fun emptyPath(): Path = Path(emptyList())

/**
 * 按照给定的顺序返回包含给定节点的路径。
 */
fun pathOf(vararg nodes: Node<*>): Path = Path(listOf(*nodes))

/**
 * 返回一个空路径。
 */
fun pathOf(): Path = emptyPath()