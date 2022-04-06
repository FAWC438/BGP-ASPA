package io

import bgp.BGPRoute
import bgp.policies.interdomain.*
import core.routing.Extender

/**
 *
 * 该文件包含一组函数和扩展方法，用于将标签转换为扩展器、成本或路由，以及用于在另一个方向转换的函数和扩展方法：从扩展器、路径花费或路由转换为标签。
 */

//
// 从字符串标签转换为类
//

/**
 * 将字符串解析为域间扩展器并返回结果。此方法区分大小写。
 *
 * 有效的字符串标签和相应的扩展器：
 *  R+ -> PeerplusExtender
 *  R* -> PeerstarExtender
 *  C  -> CustomerExtender
 *  R  -> PeerExtender
 *  P  -> ProviderExtender
 *  S  -> SiblingExtender
 *
 * @return 字符串对应的扩展器
 * @throws InvalidLabelException 如果字符串不匹配任何有效的扩展标签
 */
@Throws(InvalidLabelException::class)
fun String.toInterdomainExtender(): Extender<BGPRoute> = when (this) {

    "R+" -> PeerplusExtender
    "R*" -> PeerstarExtender
    "C" -> CustomerExtender
    "R" -> PeerExtender
    "P" -> ProviderExtender
    "S" -> SiblingExtender
    else -> throw InvalidLabelException(
        "extender label '$this' was not recognized, " +
                "it but must be one of R+, R*, C, R, P, and S"
    )
}

/**
 * 将字符串解析为域间本地首选项值并返回结果。此方法区分大小写。
 *
 * Valid string labels and corresponding local preference values:
 *  r+ -> peer+ route local preference
 *  r* -> peer* route local preference
 *  c  -> customer route local preference
 *  r  -> peer route local preference
 *  p  -> provider route local preference
 *
 * @return the local preference corresponding to the cost label
 * @throws InvalidLabelException if the string does not match any valid cost label
 */
@Throws(InvalidLabelException::class)
fun String.toInterdomainLocalPreference(): Int = when (this) {

    "r+" -> peerplusLocalPreference
    "r*" -> peerstarLocalPreference
    "c" -> customerLocalPreference
    "r" -> peerLocalPreference
    "p" -> providerLocalPreference
    else -> throw InvalidLabelException(
        "cost label '$this' was not recognized, " +
                "it but must be one of r+, r*, c, r, and p"
    )
}

/**
 * 尝试将 [label] 转换为域间扩展器。如果失败，则抛出 ParseException。
 * @see #toInterdomainExtender() 有关如何解析标签的更多详细信息。
 *
 * @param label      要解析的标签
 * @param lineNumber 找到标签的行号（仅用于解析异常消息）
 * @return [label] 对应的扩展器
 * @throws ParseException 如果标签未被识别
 */
@Throws(ParseException::class)
fun parseInterdomainExtender(label: String, lineNumber: Int = 0): Extender<BGPRoute> {

    return try {
        label.toInterdomainExtender()
    } catch (e: InvalidLabelException) {
        throw ParseException(e.message ?: "", lineNumber)
    }
}


// 此函数是必需的，因为它作为参数传递，需要具有此特定签名的函数
@Suppress("NOTHING_TO_INLINE")
@Throws(ParseException::class)
inline fun parseInterdomainExtender(label: String): Extender<BGPRoute> = parseInterdomainExtender(label, lineNumber = 0)


/**
 * 尝试将 [label] 转换为域间本地首选项值。如果失败，则抛出 ParseException。
 * @see #toInterdomainLocalPreference() 有关如何解析标签的更多详细信息。
 *
 * @param label      要解析的标签
 * @param lineNumber 找到标签的行号（仅用于解析异常消息）
 * @return [label] 对应的本地偏好值
 * @throws ParseException 如果标签未被识别
 */
@Suppress("NOTHING_TO_INLINE")
@Throws(ParseException::class)
inline fun parseInterdomainCost(label: String, lineNumber: Int): Int {

    return try {
        label.toInterdomainLocalPreference()
    } catch (e: InvalidLabelException) {
        throw ParseException(e.message ?: "", lineNumber)
    }
}

//
// 从类转换为字符串对象
//

fun Int.toInterdomainLabel(): String = when (this) {
    peerplusLocalPreference -> "r+"
    peerstarLocalPreference -> "r*"
    customerLocalPreference -> "c"
    peerLocalPreference -> "r"
    providerLocalPreference -> "p"
    BGPRoute.invalid().localPref -> BGPRoute.invalid().toString()
    BGPRoute.self().localPref -> BGPRoute.self().toString()
    else -> this.toString()
}