package core.routing

/**
 * [Extender] 是一种通过链路发送的路由转换功能。
 *
 * Extender 接口定义了一个方法 [extend]。子类应实现此方法以指定此扩展程序如何转换路由。
 *
 * 扩展器与邻居相关联。它们描述了如何在相邻节点学习在一个节点选择的路由。
 * 最终，与给定邻居相关联的扩展器对本地节点的导出策略和邻居节点的导入策略进行建模。
 *
 * @see [bgp.policies] 有关扩展器实现的示例。
 *
 */
interface Extender<R : Route> {

    /**
     * 根据这个扩展器实现的功能，取一个[route]并返回从[route]得到的扩展路由。
     * 输出路由可能取决于 [sender]，发送 [route] 的节点。
     */
    fun extend(route: R, sender: Node<R>): R

}