package utils

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 *
 * This file contains a set of helper functions and extension function.
 */

/**
 * 将字符串解析为非负整数并返回结果
 *
 * @return 非负整数
 * @throws NumberFormatException - 如果字符串不是数字的有效表示。
 */
@Throws(NumberFormatException::class)
fun String.toNonNegativeInt(): Int {

    val value = this.toInt()
    if (value < 0) {
        throw NumberFormatException("For input string \"$this\"")
    }

    return value
}
