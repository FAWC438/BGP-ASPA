package ui.cli

import bgp.BGPRoute
import core.routing.NodeID
import core.simulator.Simulator
import org.apache.commons.cli.*
import simulation.BGPAdvertisementInitializer
import simulation.Initializer
import utils.toNonNegativeInt
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class InputArgumentsParser {

    companion object {
        private const val MAIN_COMMAND = "ssbgp-simulator"

        // 信息选项
        private const val HELP = "help"
        private const val VERSION = "version"

        // 执行选项
        private const val TOPOLOGY_FILE = "topology"
        private const val ADVERTISER = "advertiser"
        private const val REPETITIONS = "repetitions"
        private const val REPORT_DIRECTORY = "output"
        private const val MIN_DELAY = "mindelay"
        private const val MAX_DELAY = "maxdelay"
        private const val THRESHOLD = "threshold"
        private const val SEED = "seed"
        private const val STUBS = "stubs"
        private const val NODE_REPORT = "reportnodes"
        private const val ADVERTISE_FILE = "advertise"
        private const val METADATA = "metadata"
        private const val TRACE = "trace"
        private const val MRAI = "mrai"
    }

    private val options = Options()

    init {

        options.apply {

            // Information Options
            addOption(
                Option.builder("h")
                    .desc("Print help")
                    .required(false)
                    .hasArg(false)
                    .longOpt(HELP)
                    .build()
            )
            addOption(
                Option.builder("V")
                    .desc("Print application's version")
                    .required(false)
                    .hasArg(false)
                    .longOpt(VERSION)
                    .build()
            )

            // Execution Options
            addOption(
                Option.builder("t")
                    .desc("Topology file to simulate with")
                    .hasArg(true)
                    .argName("topology-file")
                    .longOpt(TOPOLOGY_FILE)
                    .build()
            )
            addOption(
                Option.builder("d")
                    .desc("ID(s) of node(s) advertising a destination")
                    .hasArgs()
                    .argName("advertisers")
                    .longOpt(ADVERTISER)
                    .build()
            )
            addOption(
                Option.builder("c")
                    .desc("Number of executions to run [default: 1]")
                    .hasArg(true)
                    .argName("executions")
                    .longOpt(REPETITIONS)
                    .build()
            )
            addOption(
                Option.builder("o")
                    .desc("Directory to place reports [default: working directory]")
                    .hasArg(true)
                    .argName("out-directory")
                    .longOpt(REPORT_DIRECTORY)
                    .build()
            )
            addOption(
                Option.builder("min")
                    .desc("Minimum delay applied to the routing messages [default: 1]")
                    .hasArg(true)
                    .argName("mindelay")
                    .longOpt(MIN_DELAY)
                    .build()
            )
            addOption(
                Option.builder("max")
                    .desc("Maximum delay applied to the routing messages [default: 1]")
                    .hasArg(true)
                    .argName("maxdelay")
                    .longOpt(MAX_DELAY)
                    .build()
            )
            addOption(
                Option.builder("th")
                    .desc("Maximum amount simulation time [default: 1000000]")
                    .hasArg(true)
                    .argName("threshold")
                    .longOpt(THRESHOLD)
                    .build()
            )
            addOption(
                Option.builder("s")
                    .desc("Seed used to generate the delays in the first execution")
                    .required(false)
                    .hasArg(true)
                    .argName("threshold")
                    .longOpt(SEED)
                    .build()
            )
            addOption(
                Option.builder("S")
                    .desc("Stubs file")
                    .required(false)
                    .hasArg(true)
                    .argName("stubs-file")
                    .longOpt(STUBS)
                    .build()
            )
            addOption(
                Option.builder("rn")
                    .desc("Output data for each individual node")
                    .required(false)
                    .hasArg(false)
                    .longOpt(NODE_REPORT)
                    .build()
            )
            addOption(
                Option.builder("D")
                    .desc("File with advertisements")
                    .hasArg(true)
                    .argName("advertise-file")
                    .longOpt(ADVERTISE_FILE)
                    .build()
            )
            addOption(
                Option.builder("meta")
                    .desc("Output metadata file")
                    .hasArg(false)
                    .longOpt(METADATA)
                    .build()
            )
            addOption(
                Option.builder("tr")
                    .desc("Output a trace with the simulation events to a file")
                    .hasArg(false)
                    .longOpt(TRACE)
                    .build()
            )
            addOption(
                Option.builder("mrai")
                    .desc("Force the MRAI value for all nodes")
                    .hasArg(true)
                    .argName("<mrai>")
                    .longOpt(MRAI)
                    .build()
            )
        }

    }

    @Throws(InputArgumentsException::class)
    fun parse(args: Array<String>): Initializer<BGPRoute> {

        val commandLine = try {
            DefaultParser().parse(options, args)
        } catch (e: ParseException) {
            throw InputArgumentsException(e.message.toString())
        }

        if (commandLine.hasOption(HELP)) {
            val formatter = HelpFormatter()
            formatter.width = 100

            val usageHeader = "\nOptions:"
            formatter.printHelp(MAIN_COMMAND, usageHeader, options, "", true)
            exitProcess(0)
        }

        if (commandLine.hasOption(VERSION)) {
            println("SS-BGP Simulator: ${Simulator.version()}")
            exitProcess(0)
        }

        commandLine.let {

            //
            // 验证使用的选项
            //

            if (it.hasOption(ADVERTISER) && it.hasOption(ADVERTISE_FILE)) {
                throw InputArgumentsException("options -d/--$ADVERTISER and -D/--$ADVERTISE_FILE are mutually exclusive")
            } else if (!it.hasOption(ADVERTISER) && !it.hasOption(ADVERTISE_FILE)) {
                throw InputArgumentsException("one option of -d/--$ADVERTISER and -D/--$ADVERTISE_FILE is required")
            }

            //
            // 解析选项值
            //

            val topologyFile = getFile(it, option = TOPOLOGY_FILE).get()
            val advertisers = getManyNonNegativeIntegers(it, option = ADVERTISER, default = emptyList())
            val advertisementsFile = getFile(it, option = ADVERTISE_FILE, default = Optional.empty())
            val repetitions = getPositiveInteger(it, option = REPETITIONS, default = 1)
            val reportDirectory =
                getDirectory(it, option = REPORT_DIRECTORY, default = File(System.getProperty("user.dir")))
            val threshold = getPositiveInteger(it, option = THRESHOLD, default = 1_000_000)
            val seed = getLong(it, option = SEED, default = System.currentTimeMillis())
            val stubsFile = getFile(it, option = STUBS, default = Optional.empty())
            val reportNodes = commandLine.hasOption(NODE_REPORT)
            val minDelay = getPositiveInteger(it, option = MIN_DELAY, default = 1)
            val maxDelay = getPositiveInteger(it, option = MAX_DELAY, default = 1)
            val outputMetadata = commandLine.hasOption(METADATA)
            val outputTrace = commandLine.hasOption(TRACE)

            // 根据用户指定的是一组通告还是一个文件来选择初始化
            val initializer = if (it.hasOption(ADVERTISER)) {
                BGPAdvertisementInitializer.with(topologyFile, advertisers.toSet())
            } else {
                BGPAdvertisementInitializer.with(topologyFile, advertisementsFile.get())
            }

            return initializer.apply {
                this.repetitions = repetitions
                this.reportDirectory = reportDirectory
                this.threshold = threshold
                this.minDelay = minDelay
                this.maxDelay = maxDelay
                this.reportNodes = reportNodes
                this.outputMetadata = outputMetadata
                this.outputTrace = outputTrace
                this.stubsFile = stubsFile.orElseGet { null }
                this.seed = seed
                if (it.hasOption(MRAI))
                    this.forcedMRAI = getNonNegativeInteger(it, MRAI)
            }
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getFile(commandLine: CommandLine, option: String, default: Optional<File>? = null): Optional<File> {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)
        val file = if (value != null) Optional.of(File(value)) else default!!   // 请参阅下面的注释
        // 注意：如果选项未定义且默认值为 null，则 verify Option 方法将抛出异常

        if (file.isPresent && !file.get().isFile) {
            throw InputArgumentsException("The file specified for `$option` does not exist: ${file.get().path}")
        }

        return file
    }

    @Throws(InputArgumentsException::class)
    private fun getDirectory(commandLine: CommandLine, option: String, default: File? = null): File {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)
        val directory = if (value != null) File(value) else default!!    // 请参阅下面的注释

        // 注意：如果选项未定义且默认值为 null，则 verify Option 方法将抛出异常

        if (!directory.isDirectory) {
            throw InputArgumentsException("The directory specified for `$option` does not exist: ${directory.path}")
        }

        return directory
    }

    @Throws(InputArgumentsException::class)
    private fun getManyNonNegativeIntegers(
        commandLine: CommandLine, option: String,
        default: List<NodeID>? = null
    ): List<NodeID> {
        verifyOption(commandLine, option, default)

        val values = commandLine.getOptionValues(option)

        try {
            @Suppress("USELESS_ELVIS")
            // 尽管 IDE 无法识别它，但如果设置了该选项，'values' 实际上可以为 null。
            // getOptionValues() 的文档表明，如果未设置该选项，它将返回 null。
            return values?.map { it.toNonNegativeInt() } ?: default!!  // never null at this point!!
        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("values for '--$option' must be non-negative integer values")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getNonNegativeInteger(commandLine: CommandLine, option: String, default: Int? = null): Int {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            return value?.toNonNegativeInt() ?: default!!  // 请参阅下面的注释
            // 注意：如果选项未定义且默认值为 null，则 verify Option 方法将抛出异常

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("value for '--$option' must be a non-negative integer value")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getPositiveInteger(commandLine: CommandLine, option: String, default: Int? = null): Int {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            val intValue = value?.toInt() ?: default!!  // 请参阅下面的注释
            // 注意：如果选项未定义且默认值为 null，则 verify Option 方法将抛出异常

            if (intValue <= 0) {
                // 处理错误
                throw NumberFormatException()
            }

            return intValue

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("Parameter '$option' must be a positive integer value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getLong(commandLine: CommandLine, option: String, default: Long? = null): Long {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            return value?.toLong() ?: default!!  // 请参阅下面的注释
            // 注意：如果选项未定义且默认值为 null，则 verify Option 方法将抛出异常

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("Parameter '$option' must be a positive long value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun verifyOption(commandLine: CommandLine, option: String, default: Any?) {

        if (!commandLine.hasOption(option) && default == null) {
            throw InputArgumentsException("The parameter '$option' is missing and it is mandatory")
        }
    }
}
