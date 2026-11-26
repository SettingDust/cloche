package earth.terrarium.cloche.target.fabric

import earth.terrarium.cloche.ClochePlugin
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.target.compilation.FabricCompilation
import earth.terrarium.cloche.target.compilation.GamePart
import earth.terrarium.cloche.target.compilation.GameShards
import earth.terrarium.cloche.target.compilation.TargetCompilation
import earth.terrarium.cloche.target.compilation.TargetCompilationInfo
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import earth.terrarium.cloche.tasks.data.MetadataFileProvider
import earth.terrarium.cloche.util.withIdeaModule
import kotlinx.serialization.json.JsonObject
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.fabric.MinecraftCodevFabricPlugin
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

internal class FabricCompilationInfo(
    name: String,
    target: FabricTargetImpl,
    intermediaryMinecraftClasspath: FileCollection,
    val splitNamedGames: GameShards.Split<Provider<RegularFile>>,
    runtimeExtraMinecraftFiles: Map<String, Provider<RegularFile>>,
    extraClasspathFiles: Provider<List<RegularFile>>,
    data: Boolean,
    test: Boolean,
    primaryGame: Provider<GamePart>,
    preapplicationSide: Provider<String>,
    preapplicationLibraries: FileCollection,
) : TargetCompilationInfo<FabricTargetImpl>(
        name,
        target,
        intermediaryMinecraftClasspath,
        splitNamedGames,
        primaryGame,
        runtimeExtraMinecraftFiles,
        extraClasspathFiles,
        primaryGame.map { if (it == GamePart.CLIENT) ModDistribution.client else ModDistribution.common },
        data,
        test,
        IncludeTransformationStateAttribute.Stripped,
        JarInJar::class.java,
        preapplicationLibraries,
        preapplicationSide,
    )

internal abstract class FabricCompilationImpl @Inject constructor(override val info: FabricCompilationInfo) :
    TargetCompilation<FabricTargetImpl>(info), FabricCompilation {
    override val generateMetadataTask = project.tasks.register<GenerateFabricModJson>(
        lowerCamelCaseGradleName("generate", target.featureName, featureName, "modJson"),
    ) {
        loaderDependencyVersion.set(target.loaderVersion.map {
            it.substringBeforeLast('.')
        })

        output.set(metadataDirectory.map {
            it.file(MinecraftCodevFabricPlugin.MOD_JSON)
        })

        metadata.set(target.metadata)

        mixinConfigs.from(mixins)
    }

    init {
        if (!info.name.startsWith(ClochePlugin.CLIENT_COMPILATION_NAME)) {
            project.withIdeaModule(sourceSet) {
                it.resourceDirs.add(metadataDirectory.get().asFile)
            }
        }
    }

    override fun withMetadataJson(action: Action<MetadataFileProvider<JsonObject>>) = generateMetadataTask.configure {
        withJson(action)
    }
}
