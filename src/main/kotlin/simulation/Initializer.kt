package simulation

import core.routing.Route
import ui.Application

/**
 *
 * 初始化程序负责设置模拟器并使其准备好运行。为此，它可能需要一些应在构造函数中提供的参数。
 *
 * TODO @doc - improve the initializer's documentation
 */
interface Initializer<R : Route> {

    /**
     * 根据一些预定义的参数初始化运行器和执行器。
     */
    fun initialize(application: Application, metadata: Metadata): Pair<Runner<R>, Execution<R>>
}