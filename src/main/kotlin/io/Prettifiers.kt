package io

import bgp.BGPRoute
import core.routing.Node
import core.routing.Path
import core.routing.Route

/**
 *
 * 该文件包含扩展方法来美化一些输出给用户的类。
 */

/**
 * 以字符串形式返回节点的 ID。
 */
fun <R: Route> Node<R>.pretty(): String = id.toString()

/**
 * 返回以逗号分隔的路径中节点的 ID。
 */
fun Path.pretty(): String = joinToString(transform = {it.pretty()})

/**
 * 将 BGP 路由的本地优先级转换为相应的域间标签，将 AS 路径转换为路径。这些放在括号内并用逗号分隔。
 */
fun BGPRoute.pretty(): String {

    if (this === BGPRoute.invalid() || this === BGPRoute.self()) {
        return toString()
    }

    return "(${localPref.toInterdomainLabel()}, ${asPath.pretty()})"
}