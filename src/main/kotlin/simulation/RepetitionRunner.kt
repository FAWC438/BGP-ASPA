package simulation

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.DelayGenerator
import core.simulator.Simulator
import core.simulator.Time
import io.KeyValueWriter
import ui.Application
import java.io.File
import java.time.Instant


class RepetitionRunner<R : Route>(
    private val application: Application,
    private val topology: Topology<R>,
    private val advertisements: List<Advertisement<R>>,
    private val threshold: Time,
    private val repetitions: Int,
    private val messageDelayGenerator: DelayGenerator,
    private val metadataFile: File?

) : Runner<R> {

    /**
     * 按照 [repetitions] 属性中指定的次数运行指定的执行。
     *
     * 模拟器的配置可能在运行期间被修改。在这个方法结束时，模拟器总是恢复到它的默认值。
     *
     * @param execution 将在每次运行中执行的执行
     * @param metadata  可能已经包含一些元值的元数据实例
     */
    override fun run(execution: Execution<R>, metadata: Metadata) {

        val startInstant = Instant.now()
        Simulator.messageDelayGenerator = messageDelayGenerator

        application.run {

            try {
                repeat(times = repetitions) { repetition ->

                    application.execute(repetition + 1, advertisements, messageDelayGenerator.seed) {
                        execution.execute(topology, advertisements, threshold)
                    }

                    // 为下一次执行清理
                    topology.reset()
                    // TODO @refactor - put stubs in the topology itself to avoid having this
                    //                  reset() method in the advertiser interface
                    advertisements.forEach { it.advertiser.reset() }
                    Simulator.messageDelayGenerator.generateNewSeed()
                }

            } finally {
                // 确保模拟器在运行后始终恢复为默认值
                Simulator.resetToDefaults()
            }
        }

        metadata["Start Time"] = startInstant
        metadata["Finish Time"] = Instant.now()

        if (metadataFile != null) {
            application.writeMetadata(metadataFile) {

                KeyValueWriter(metadataFile).use {
                    for ((key, value) in metadata) {
                        it.write(key, value)
                    }
                }
            }
        }

    }
}