package core.routing

/**
 * 包含路由消息中包含的所有信息的数据类。
 *
 * 参与分布式路由协议的节点与相邻节点交换包含路由信息的路由，以相互提供连接。路由由消息携带。
 *
 *与任何消息一样，路由消息包含消息的 [sender] 和 [recipient]。最重要的是，一条消息携带一条[route]，由[sender]发送给[recipient]。
 *
 * @property sender    发送消息的节点
 * @property recipient 接收消息的节点
 * @property route     发送者发送的路由
 *
 */
data class Message<R : Route>(
    val sender: Node<R>,
    val recipient: Node<R>,
    val route: R
)