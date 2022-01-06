package ui

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import java.io.File

/**
 * 命令行应用程序接口
 */
interface Application {

    fun launch(args: Array<String>)

    /**
     * 加载拓扑时调用
     *
     * @param topologyFile 拓扑文件的文件路径
     * @param block        加载拓扑的代码块
     */
    fun <R : Route> loadTopology(topologyFile: File, block: () -> Topology<R>): Topology<R>

    /**
     * 在设置要在模拟中发生的路由通告时调用。这可能意味着访问文件系统，这可能会引发一些 IO 错误。
     *
     * @param block 设置路由通告的代码块
     * @return 包含已设置路由通告的列表
     */
    fun <R : Route> setupAdvertisements(block: () -> List<Advertisement<R>>): List<Advertisement<R>>

    /**
     * 在读取存根文件时调用。它返回 [block] 返回的任何内容。
     *
     * @param file  将要读取的存根文件，null 表示文件未被读取。
     * @param block 读取存根文件的代码块
     * @return [block] 的返回内容。
     */
    fun <T> readStubsFile(file: File?, block: () -> T): T

    /**
     * 在读取路由通告文件时调用。它返回 [block] 返回的任何内容。
     *
     * @param file  将要读取的路由通告文件
     * @param block 读取存根文件的代码块
     * @return [block] 的返回内容。
     */
    fun <T> readAdvertisementsFile(file: File, block: () -> T): T

    /**
     * 在每次执行时调用。
     *
     * @param executionID    执行的标识符
     * @param advertisements 在模拟中出现的人为设定的路由通告
     * @param seed           用于执行的消息延迟生成器的随机种子
     * @param block          执行一次程序的代码块
     */
    fun <R : Route> execute(
        executionID: Int, advertisements: List<Advertisement<R>>, seed: Long,
        block: () -> Unit
    )

    /**
     * 在一次运行期间调用。
     */
    fun run(runBlock: () -> Unit)

    /**
     * 在将元数据写入磁盘时调用。
     *
     * @param file 将要写入元数据的文件
     */
    fun writeMetadata(file: File, block: () -> Unit)

}