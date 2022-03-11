package ui.cli

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Simulator
import io.ParseException
import simulation.InitializationException
import simulation.Metadata
import ui.Application
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.system.exitProcess

/**
 * 命令行界面应用程序
 */


object CLIApplication : Application {

    private val console = Console()

    override fun launch(args: Array<String>) {

        try {
            val initializer = InputArgumentsParser().parse(args)
            val metadata = Metadata(version = Simulator.version())
            val (runner, execution) = initializer.initialize(this, metadata)
            runner.run(execution, metadata)

        } catch (e: InputArgumentsException) {
            console.error("Input arguments are invalid")
            console.error("Cause: ${e.message ?: "No information available"}")
            console.info("Try the '-h' option to see more information")
            exitProcess(1)

        } catch (e: InitializationException) {
            console.error("Initialization failed")
            console.error("Cause: ${e.message ?: "No information available"}")
            exitProcess(1)

        } catch (e: Exception) {
            console.error("Program was interrupted due to unexpected error: ${e.javaClass.simpleName}")
            console.error("Cause: ${e.message ?: "No information available"}")
            exitProcess(1)
        }
    }

    /**
     * 在加载拓扑时调用。
     *
     * @param topologyFile   将加载的拓扑文件的路径
     * @param block      加载拓扑的代码块。
     */
    override fun <R : Route> loadTopology(
        topologyFile: File,
        block: () -> Topology<R>
    ): Topology<R> {

        try {
            console.info("Topology file: ${topologyFile.path}.")
            console.info("Loading topology...  ", inline = true)

            val (duration, topology) = timer {
                block()
            }

            console.print("loaded in $duration seconds")
            return topology

        } catch (exception: ParseException) {
            console.print() // must print a new line here
            console.error("Failed to load topology due to parse error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.print() // must print a new line here
            console.error("Failed to load topology due to IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(2)
        }

    }

    /**
     * 在设置要在模拟中发生的路由通告时调用。这可能意味着访问文件系统，这可能会引发一些 IO 错误。
     *
     * @param block 设置路由通告的代码块
     * @return 包含已设置的路由通告的列表
     */
    override fun <R : Route> setupAdvertisements(block: () -> List<Advertisement<R>>): List<Advertisement<R>> {

        try {
            console.info("Setting up advertisements...  ")
            val advertisements = block()
            console.info("Advertising nodes: ${advertisements.map { it.advertiser.id }.joinToString()}")

            return advertisements

        } catch (exception: InitializationException) {
            console.print() // must print a new line here
            console.error("Failed to initialize the simulation")
            console.error("Cause: ${exception.message ?: "no information available"}")
            exitProcess(3)
        }
    }

    /**
     * 在读取存根文件时调用。它返回 [block] 返回的任何内容。
     *
     * @param file  将要读取的存根文件
     * @param block 读取存根文件的代码块
     * @return  [block] 返回的任何内容。
     */
    override fun <T> readStubsFile(file: File?, block: () -> T): T {

        return if (file == null) {
            // 文件不会被读取
            block()
        } else {
            handleReadingFiles(file, block, name = "stubs")
        }
    }

    /**
     * 在读取路由通告文件时调用。它返回 [block] 返回的任何内容。
     *
     * @param file  将要读取的路由通告文件
     * @param block 读取存根文件的代码块
     * @return [block] 返回的任何内容。
     */
    override fun <T> readAdvertisementsFile(file: File, block: () -> T): T =
        handleReadingFiles(file, block, name = "advertisements")

    /**
     * 在读取输入文件时处理错误并显示读取文件所花费的时间。
     */
    private fun <T> handleReadingFiles(file: File, block: () -> T, name: String): T {

        try {
            console.info("Reading $name file '${file.name}'...  ", inline = true)
            val (duration, value) = timer {
                block()
            }
            console.print("done in $duration seconds")

            return value

        } catch (exception: ParseException) {
            console.print() // 必须在这里打印一个新行
            console.error("Failed to parse $name file '${file.name}'")
            console.error("Cause: ${exception.message ?: "no information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.print() // 必须在这里打印一个新行
            console.error("Failed to access $name file '${file.name}' due to an IO error")
            console.error("Cause: ${exception.message ?: "no information available"}")
            exitProcess(1)
        }
    }

    /**
     * 在执行每次执行时调用。
     *
     * @param executionID    执行的标识符
     * @param advertisements 执行过程中会出现的路由通告
     * @param seed           用于执行的消息延迟生成器的种子
     * @param block          执行一次程序的代码块
     */
    override fun <R : Route> execute(
        executionID: Int, advertisements: List<Advertisement<R>>,
        seed: Long, block: () -> Unit
    ) {

        console.info("Executing $executionID (seed=$seed)...  ", inline = true)
        val (duration, _) = timer {
            block()
        }
        console.print("finished in $duration seconds")
    }

    /**
     * 在一次运行期间调用。
     */
    override fun run(runBlock: () -> Unit) {

        try {
            console.info("Running...")
            val (duration, _) = timer {
                runBlock()
            }
            console.info("Finished run in $duration seconds")

        } catch (exception: IOException) {
            console.error("Failed to report results due to an IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(4)
        }

    }

    /**
     * 在将元数据写入磁盘时调用。
     *
     * @param file 将要写入元数据的文件
     */
    override fun writeMetadata(file: File, block: () -> Unit) {

        try {
            console.info("Writing metadata...  ", inline = true)
            val (duration, _) = timer {
                block()
            }
            console.print("done in $duration seconds")

        } catch (exception: IOException) {
            console.print() // 必须在这里打印一个新行
            console.error("Failed to metadata due to an IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(4)
        }
    }

}


// TODO @refactor - move timer to a utils file
private fun <R> timer(block: () -> R): Pair<Double, R> {

    val start = Instant.now()
    val value = block()
    val end = Instant.now()

    return Pair(Duration.between(start, end).toMillis().div(1000.0), value)
    // return Pair(Duration.between(start, end).toMillis().div(1.0), value)
}