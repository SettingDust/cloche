package earth.terrarium.cloche.remap

import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.msrandom.minecraftcodev.core.utils.cacheExpensiveOperation
import net.msrandom.minecraftcodev.core.utils.getAsPath
import net.msrandom.minecraftcodev.core.utils.zipFileSystem
import net.msrandom.minecraftcodev.includes.includedJarListingRules
import net.msrandom.minecraftcodev.remapper.JarRemapper
import net.msrandom.minecraftcodev.remapper.MappingResolutionData
import net.msrandom.minecraftcodev.remapper.MappingTreeProvider
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.REMAP_OPERATION_VERSION
import net.msrandom.minecraftcodev.remapper.isMod
import net.msrandom.minecraftcodev.remapper.loadMappingFile
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.InputArtifactDependencies
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.ExecOperations
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlin.io.path.copyTo
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

abstract class ClocheRemapAction : TransformAction<ClocheRemapAction.Parameters> {
    abstract class Parameters : TransformParameters {
        abstract val mappingArtifacts: ConfigurableFileCollection
            @InputFiles
            @PathSensitive(PathSensitivity.NONE)
            get

        abstract val javaExecutable: RegularFileProperty
            @Internal get

        abstract val versionManifestUrl: Property<String>
            @Internal get

        abstract val offline: Property<Boolean>
            @Internal get

        abstract val sourceNamespace: Property<String>
            @Input get

        abstract val targetNamespace: Property<String>
            @Input get

        abstract val extraClasspath: ConfigurableFileCollection
            @CompileClasspath
            @InputFiles
            get

        abstract val filterMods: Property<Boolean>
            @Input get

        abstract val modFiles: ConfigurableFileCollection
            @PathSensitive(PathSensitivity.NONE)
            @InputFiles
            get

        abstract val nonModFiles: ConfigurableFileCollection
            @PathSensitive(PathSensitivity.NONE)
            @InputFiles
            get

        abstract val cacheDirectory: DirectoryProperty
            @Internal get

        init {
            targetNamespace.convention(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            filterMods.convention(true)
        }
    }

    abstract val inputFile: Provider<FileSystemLocation>
        @InputArtifact
        @PathSensitive(PathSensitivity.NONE)
        get

    abstract val classpath: FileCollection
        @Classpath
        @InputArtifactDependencies
        get

    abstract val execOperations: ExecOperations
        @Inject get

    override fun transform(outputs: TransformOutputs) {
        val input = inputFile.get().asFile

        if (parameters.filterMods.get() && (input in parameters.nonModFiles || (input !in parameters.modFiles && !isMod(input.toPath())))) {
            outputs.file(inputFile)

            return
        }

        val sourceNamespace = parameters.sourceNamespace.get()
        val targetNamespace = parameters.targetNamespace.get()

        val output = outputs.file("${input.nameWithoutExtension}-$targetNamespace.${input.extension}")

        val classpath = (classpath + parameters.modFiles + parameters.extraClasspath.filter { it.isFile }) - input
        val inputPath = input.toPath()

        val cacheKey = buildList<Path> {
            addAll(parameters.mappingArtifacts.map { it.toPath() })
            add(inputPath)
        }

        cacheExpensiveOperation(parameters.cacheDirectory.getAsPath(), "remap-$REMAP_OPERATION_VERSION", cacheKey, output.toPath()) { (output) ->
            println("Remapping mod $input from $sourceNamespace to $targetNamespace")

            val mappings = loadMappings()

            JarRemapper.remap(
                mappings,
                sourceNamespace,
                targetNamespace,
                inputPath,
                output,
                classpath,
            )

            zipFileSystem(output).use { outputFs ->
                val root = outputFs.getPath("/")
                val handler = includedJarListingRules.firstNotNullOfOrNull { it.load(root) }
                if (handler != null) {
                    for (includedJar in handler.list(root)) {
                        val path = outputFs.getPath(includedJar)

                        val nestedCacheKey = buildList<Path> {
                            addAll(parameters.mappingArtifacts.map { it.toPath() })
                            add(path)
                        }

                        cacheExpensiveOperation(
                            parameters.cacheDirectory.getAsPath(),
                            "remap-$REMAP_OPERATION_VERSION",
                            nestedCacheKey,
                            path,
                        ) { (output) ->
                            println("Remapping ${input.name} nested jar $includedJar from $sourceNamespace to $targetNamespace")

                            val nestedInput = Files.createTempDirectory("nested-jar-remap").resolve("${path.nameWithoutExtension}-$targetNamespace.${path.extension}")

                            path.copyTo(nestedInput, StandardCopyOption.REPLACE_EXISTING)

                            JarRemapper.remap(
                                mappings,
                                sourceNamespace,
                                targetNamespace,
                                nestedInput,
                                output,
                                classpath,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadMappings() = MemoryMappingTree().also { tree ->
        val data = MappingResolutionData(
            MappingTreeProvider(tree),
            parameters.javaExecutable.get(),
            parameters.mappingArtifacts,
            parameters.cacheDirectory.getAsPath(),
            parameters.versionManifestUrl.get(),
            parameters.offline.get(),
            execOperations,
        )

        for (file in parameters.mappingArtifacts) {
            loadMappingFile(file.toPath(), data)
        }
    }
}
