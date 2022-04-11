package simulation

import bgp.BGP
import bgp.BGPRoute
import core.routing.NodeID
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.RandomDelayGenerator
import core.simulator.Time
import io.InterdomainAdvertisementReader
import io.InterdomainTopologyReader
import io.ParseException
import io.parseInterdomainExtender
import ui.Application
import java.io.File
import java.io.IOException


sealed class BGPAdvertisementInitializer(
    // 必须的
    private val topologyFile: File,

    // Optional (with defaults)
    var repetitions: Int = DEFAULT_REPETITIONS,
    var minDelay: Time = DEFAULT_MINDELAY,
    var maxDelay: Time = DEFAULT_MAXDELAY,
    var threshold: Time = DEFAULT_THRESHOLD,
    var reportDirectory: File = DEFAULT_REPORT_DIRECTORY,
    var reportNodes: Boolean = false,
    var outputMetadata: Boolean = false,
    var outputTrace: Boolean = false,

    // Optional (without defaults)
    var seed: Long? = null,
    var stubsFile: File? = null,
    var forcedMRAI: Time? = null

) : Initializer<BGPRoute> {

    companion object {

        const val DEFAULT_REPETITIONS = 1
        const val DEFAULT_THRESHOLD = 1_000_000
        const val DEFAULT_MINDELAY = 1
        const val DEFAULT_MAXDELAY = 1
        val DEFAULT_REPORT_DIRECTORY = File(System.getProperty("user.dir"))  // 当前工作目录

        fun with(topologyFile: File, advertiserIDs: Set<NodeID>): BGPAdvertisementInitializer =
            UsingDefaultSet(topologyFile, advertiserIDs)

        fun with(topologyFile: File, advertisementsFile: File): BGPAdvertisementInitializer =
            UsingFile(topologyFile, advertisementsFile)
    }

    /**
     * 这是报告文件的基本输出名称。基本输出名称不包含扩展名。子类应根据其规范提供名称。
     */
    abstract val outputName: String

    /**
     * 初始化模拟。它设置要运行的可执行程序和运行它们的运行器。
     */
    override fun initialize(application: Application, metadata: Metadata): Pair<Runner<BGPRoute>, Execution<BGPRoute>> {

        // 如果没有设置种子，则基于当前时间为每个新的初始化生成一个新的种子
        val seed = seed ?: System.currentTimeMillis()

        // 根据文件类型附加扩展名
        val basicReportFile = File(reportDirectory, outputName.plus(".basic.csv"))
        val nodesReportFile = File(reportDirectory, outputName.plus(".nodes.csv"))
        val metadataFile = File(reportDirectory, outputName.plus(".meta.txt"))
        val traceFile = File(reportDirectory, outputName.plus(".trace.txt"))

        // 设置消息延迟生成器
        val messageDelayGenerator = try {
            RandomDelayGenerator.with(minDelay, maxDelay, seed)
        } catch (e: IllegalArgumentException) {
            throw InitializationException(e.message)
        }

        // 加载拓扑
        val topology: Topology<BGPRoute> = application.loadTopology(topologyFile) {
            InterdomainTopologyReader(topologyFile, forcedMRAI).use {
                it.read()
            }
        }

        val advertisements = application.setupAdvertisements {
            // 子类决定了通告的配置方式，请参阅此文件底部的子类
            initAdvertisements(application, topology)
        }

        // 一次执行需要的参数
        val runner = RepetitionRunner(
            application,
            topology,
            advertisements,
            threshold,
            repetitions,
            messageDelayGenerator,
            metadataFile = if (outputMetadata) metadataFile else null  // null 告诉程序不要打印元数据
        )

        // 模拟执行入口，即每次重复执行的入口
        val execution = SimpleAdvertisementExecution<BGPRoute>().apply {
            dataCollectors.add(BasicDataCollector(basicReportFile))

            if (reportNodes) {
                dataCollectors.add(NodeDataCollector(nodesReportFile))
            }

            if (outputTrace) {
                dataCollectors.add(TraceReporter(traceFile))
            }
        }

        metadata["Topology file"] = topologyFile.name
        stubsFile?.apply {
            metadata["Stubs file"] = name
        }
        metadata["Advertiser(s)"] = advertisements.map { it.advertiser.id }.joinToString()
        metadata["Minimum Delay"] = minDelay.toString()
        metadata["Maximum Delay"] = maxDelay.toString()
        metadata["Threshold"] = threshold.toString()
        forcedMRAI?.apply {
            metadata["MRAI"] = forcedMRAI.toString()
        }

        return Pair(runner, execution)
    }

    /**
     * 子类应使用此方法来初始化通告以在模拟中发生。这些定义的方式取决于实现。
     *
     * @return 模拟中出现的初始化通告列表
     */
    protected abstract fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
            : List<Advertisement<BGPRoute>>

    // -----------------------------------------------------------------------------------------------------------------
    //
    //  Subclasses
    //
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 基于一组预定义的通告客户 ID 进行初始化。每个 ID 都映射到一个通告者。可以从拓扑或存根文件中获取通告者。
     *
     * 它为每个通告者生成一个通告，默认路由对应self BGP route，advertising time为0。
     */
    private class UsingDefaultSet(topologyFile: File, val advertiserIDs: Set<NodeID>) :
        BGPAdvertisementInitializer(topologyFile) {

        init {
            // 验证构造函数中是否提供了至少 1 个通告者 ID
            if (advertiserIDs.isEmpty()) {
                throw IllegalArgumentException("initializer requires at least 1 advertiser")
            }
        }

        /**
         * 输出名称（不包括扩展名）对应于拓扑文件名和通告者的 ID。
         * 例如，如果拓扑文件名为“topology.topo”，通告者 ID 为 10 和 12，则输出文件名为“topology_10-12”。
         */
        override val outputName: String = topologyFile.nameWithoutExtension + "_${advertiserIDs.joinToString("-")}"

        /**
         * 为 ID 集中指定的每个广告商创建一个通告。
         *
         * @throws InitializationException if the advertisers can not be found in the topology or stubs file
         * @throws ParseException if the stubs file format is invalid
         * @throws IOException if an IO error occurs
         * @return 模拟中出现的初始化通告列表
         */
        override fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
                : List<Advertisement<BGPRoute>> {

            // 从指定 ID 中查找所有通告者
            val advertisers = application.readStubsFile(stubsFile) {
                AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                    .get(advertiserIDs.toList())
            }

            // 该模式下，节点将自身BGP路由设置为默认路由
            // 使用通告文件配置不同的路由
            return advertisers.map { Advertisement(it, BGPRoute.self()) }.toList()
        }
    }

    /**
     * 基于通告文件的初始化。该文件描述了在每次模拟执行中出现的一组通告。
     *
     * 通告文件中描述的通告者 ID 映射到实际通告者。可以从拓扑或存根文件中获取通告者。
     */
    private class UsingFile(topologyFile: File, val advertisementsFile: File) :
        BGPAdvertisementInitializer(topologyFile) {

        /**
         * 输出名称（不包括扩展名）对应于带有通告文件名的拓扑文件名。
         *
         * 例如，如果拓扑文件名为“topology.topo”，广告文件名为“advertisements.adv”，则输出基本名称为“topology-advertisements”
         */
        override val outputName: String =
            "${topologyFile.nameWithoutExtension}-${advertisementsFile.nameWithoutExtension}"

        /**
         * 通告是从通告文件中获取的
         *
         * @return 模拟中出现的初始化通告列表
         * @throws InitializationException if the advertisers can not be found in the topology or stubs file
         * @throws ParseException if the advertisements file format or the stubs file format are invalid
         * @throws IOException if an IO error occurs
         */
        @Throws(InitializationException::class, ParseException::class, IOException::class)
        override fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
                : List<Advertisement<BGPRoute>> {

            val advertisingInfo = application.readAdvertisementsFile(advertisementsFile) {
                InterdomainAdvertisementReader(advertisementsFile).use {
                    it.read()
                }
            }

            // 根据通告文件中包含的 ID 查找所有广告商
            val advertisers = application.readStubsFile(stubsFile) {
                AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                    .get(advertisingInfo.map { it.advertiserID })
            }

            val advertisersByID = advertisers.associateBy { it.id }

            return advertisingInfo.map {
                val advertiser = advertisersByID[it.advertiserID] ?: throw IllegalStateException("can not happen")
                Advertisement(advertiser, it.defaultRoute, it.time)
            }
        }
    }
}