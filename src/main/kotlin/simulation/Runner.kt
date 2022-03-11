package simulation

import core.routing.Route

/**
 *
 * TODO @doc - add documentation for Runner
 */
interface Runner<R : Route> {

    /**
     * 运行指定的执行程序。
     */
    fun run(execution: Execution<R>, metadata: Metadata)

}