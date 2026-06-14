package earth.terrarium.cloche.tasks

import earth.terrarium.cloche.remap.RemapReport
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class ClocheRemapReport : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val report: Property<RemapReport>

    init {
        group = "cloche diagnostics"
        description = "Writes Cloche remap namespace diagnostics reports."
    }

    @TaskAction
    fun writeReport() {
        val report = report.get()
        val directory = outputDirectory.get().asFile
        directory.mkdirs()
        directory.resolve("report.json").writeText(report.toJson())
        directory.resolve("report.txt").writeText(report.toText())
    }
}
