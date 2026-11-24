package earth.terrarium.cloche.target.compilation

import earth.terrarium.cloche.NO_NAME_MAPPING_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.mixins.StripMixins
import net.msrandom.minecraftcodev.mixins.task.Mixin
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.isMod
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerTransform

private abstract class SelectMixinSources : TransformAction<SelectMixinSources.Parameters> {
    interface Parameters : StripMixins.Parameters {
        @get:InputFiles
        @get:PathSensitive(PathSensitivity.ABSOLUTE)
        val selectedSources: ConfigurableFileCollection
    }

    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val inputFile: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputFile.get().asFile
        if (input !in parameters.selectedSources) {
            outputs.file(input)
            return
        }
        val source = inputFile
        val records = parameters
        object : StripMixins() {
            override val inputFile = source

            override fun getParameters(): StripMixins.Parameters = records
        }.transform(outputs)
    }
}

internal val MIXIN_STATE_ATTRIBUTE: Attribute<String> =
    Attribute.of("earth.terrarium.cloche.mixinPreapplication", String::class.java)
internal const val RAW_MIXIN_STATE = "raw"

internal fun TargetCompilation<*>.dependOnPreapplication(task: Task) {
    task.dependsOn(compilationConsumption.preparation)
}

private fun <T, R> mapPreviewGames(
    games: GameShards<T>,
    transform: (GamePart, T) -> R,
): GameShards<R> =
    when (games) {
        is GameShards.Joined -> GameShards.Joined(transform(GamePart.JOINED, games.value))
        is GameShards.Split -> GameShards.Split(transform(GamePart.COMMON, games.common), transform(GamePart.CLIENT, games.client))
    }

internal fun <T> gamesInCompileOrder(
    games: GameShards<T>,
    side: String,
): List<T> =
    when (side) {
        "SERVER" -> {
            require(games is GameShards.Split) { "SERVER preview requires Split Minecraft" }
            listOf(games.common)
        }

        "CLIENT" -> {
            when (games) {
                is GameShards.Joined -> listOf(games.value)
                is GameShards.Split -> listOf(games.client, games.common)
            }
        }

        else -> {
            throw IllegalArgumentException("Unknown Mixin side: $side")
        }
    }

internal fun <T> gamesInRecordOrder(
    games: GameShards<T>,
    side: String,
): List<T> =
    when (side) {
        "SERVER" -> {
            require(games is GameShards.Split) { "SERVER preview requires Split Minecraft" }
            listOf(games.common)
        }

        "CLIENT" -> {
            when (games) {
                is GameShards.Joined -> listOf(games.value)
                is GameShards.Split -> listOf(games.common, games.client)
            }
        }

        else -> {
            throw IllegalArgumentException("Unknown Mixin side: $side")
        }
    }

internal data class MixinPreview(
    val tasks: GameShards<TaskProvider<Mixin>>,
    val strippedGames: GameShards<Provider<RegularFile>>,
    val operations: Provider<List<TaskProvider<Mixin>>>,
    val records: Provider<List<RegularFile>>,
)

internal data class MinecraftSelection(
    val games: Provider<List<RegularFile>>,
    val stubGames: Provider<List<RegularFile>>,
    val source: Provider<RegularFile>,
    val libraries: FileCollection,
    val operations: Provider<List<TaskProvider<Mixin>>>,
)

internal data class CompilationConsumption(
    val artifacts: Provider<ArtifactView>,
    val games: Provider<List<RegularFile>>,
    val stubGames: Provider<List<RegularFile>>,
    val preparation: FileCollection,
)

internal fun Project.registerMixinPreview(
    targetName: String?,
    compilationName: String?,
    operationPrefix: String,
    outputDirectory: Provider<Directory>,
    configuration: NamedDomainObjectProvider<Configuration>,
    modRole: Provider<out Configuration>,
    ordinaryRole: Provider<out Configuration>,
    runtimeFiles: FileCollection,
    awGames: GameShards<Provider<RegularFile>>,
    side: Provider<String>,
    enabled: Provider<Boolean>,
    libraries: FileCollection,
    remapNamespace: Provider<String>,
    mappings: Provider<RegularFile>,
): MixinPreview {
    val operation = side.map { "$operationPrefix:$it" }
    val externalFiles = files(getRawNonProjectArtifacts(configuration).map { it.files })
    val explicitMods = getRawRoleFiles(configuration, modRole)
    val ordinary = getRawRoleFiles(configuration, ordinaryRole)
    val sources =
        files(
            externalFiles.elements.map { inputs ->
                inputs.filter { input ->
                    val file = input.asFile
                    file !in ordinary && (file in explicitMods || isMod(file.toPath()))
                }
            },
        ).builtBy(externalFiles, explicitMods, ordinary)

    val tasks =
        mapPreviewGames(awGames) { part, targetFile ->
            tasks.register<Mixin>(
                lowerCamelCaseGradleName("preapply", targetName, compilationName, part.key, "mixins"),
            ) {
                group = "minecraft-transforms"
                inputFile.set(targetFile)
                mixinFiles.from(sources)
                // Source configs keep precedence over same-named resources in the raw game shards.
                classpath.from(externalFiles)
                classpath.from(side.map { gamesInCompileOrder(awGames, it) })
                classpath.from(side.map { if (it == "CLIENT") libraries else files() })
                classpath.from(runtimeFiles)
                this.side.set(side)
                val directory = outputDirectory.map { it.dir(part.key) }
                outputFile.set(directory.map { it.file("preapplied.jar") })
                appliedMixins.set(directory.map { it.file("applied-mixins.json") })
            }
        }
    val outputs = mapPreviewGames(tasks) { _, task -> task.flatMap(Mixin::outputFile) }
    val recordFiles = mapPreviewGames(tasks) { _, task -> task.flatMap(Mixin::appliedMixins) }
    val operations = side.map { gamesInRecordOrder(tasks, it) }
    val records =
        side.flatMap { selectedSide ->
            objects.listProperty(RegularFile::class.java).apply {
                gamesInRecordOrder(recordFiles, selectedSide).forEach { add(it) }
            }
        }

    configuration.configure {
        withDependencies {
            if (enabled.get()) attributes.attributeProvider(MIXIN_STATE_ATTRIBUTE, operation)
        }
    }

    val strippedGames =
        mapPreviewGames(outputs) { part, output ->
            val gameType = operation.map { "cloche-mixin-game:$it:${part.key}" }
            val game = configurations.detachedConfiguration(dependencies.create(files(output)))
            val cleanGame =
                game.incoming
                    .artifactView {
                        attributes.attributeProvider(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, gameType)
                    }.files
            afterEvaluate {
                dependencies.registerTransform(StripMixins::class) {
                    from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, gameType.get())
                    parameters.removalsFile.from(records)
                }
            }
            layout.file(cleanGame.elements.map { it.single().asFile })
        }

    afterEvaluate {
        val namespace = remapNamespace.get()
        if (namespace.isNotEmpty()) {
            mapPreviewGames(tasks) { _, task ->
                task.configure {
                    mappingFile.set(mappings)
                    sourceNamespace.set(namespace)
                    targetNamespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                }
            }
        }
        dependencies.registerTransform(SelectMixinSources::class) {
            from.attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
            // Select only after the normal remap/include/service pipeline reached C's final state.
            val attrs = configuration.get().attributes
            for (attribute in listOf(REMAPPED_ATTRIBUTE, NO_NAME_MAPPING_ATTRIBUTE)) {
                attrs.getAttribute(attribute)?.let { from.attribute(attribute, it) }
            }
            attrs.getAttribute(IncludeTransformationStateAttribute.ATTRIBUTE)?.let {
                from.attribute(IncludeTransformationStateAttribute.ATTRIBUTE, it)
            }
            to.attribute(MIXIN_STATE_ATTRIBUTE, operation.get())
            parameters.selectedSources.from(sources)
            parameters.removalsFile.from(records)
        }
    }
    return MixinPreview(tasks, strippedGames, operations, records)
}
