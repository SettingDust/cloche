package earth.terrarium.cloche.target.fabric

import earth.terrarium.cloche.ClochePlugin
import earth.terrarium.cloche.api.target.compilation.FabricSecondarySourceSets
import earth.terrarium.cloche.target.LazyConfigurableInternal
import earth.terrarium.cloche.target.compilation.GamePart
import earth.terrarium.cloche.target.compilation.TargetCompilation
import earth.terrarium.cloche.target.lazyConfigurable
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class FabricClientSecondarySourceSets @Inject constructor(info: FabricCompilationInfo) : TargetCompilation<FabricTargetImpl>(info), FabricSecondarySourceSets {
    override val metadataDirectory
        get() = target.main.metadataDirectory

    override val generateMetadataTask
        get() = target.main.generateMetadataTask

    override val data: LazyConfigurableInternal<FabricCompilationImpl> = project.lazyConfigurable {
        val data = project.objects.newInstance<FabricCompilationImpl>(
            FabricCompilationInfo(
                ClochePlugin.CLIENT_DATA_COMPILATION_NAME,
                target,
                info.intermediaryMinecraftClasspath,
                info.splitNamedGames,
                info.runtimeExtraMinecraftFiles,
                info.extraClasspathFiles,
                data = true,
                test = false,
                primaryGame = project.provider { GamePart.CLIENT },
                preapplicationSide = info.preapplicationSide,
                preapplicationLibraries = info.preapplicationLibraries,
            ),
        )

        target.data.onConfigured {
            it.generateMetadataTask.configure {
                clientMixinConfigs.from(data.mixins)
            }
        }

        data
    }

    override val test: LazyConfigurableInternal<FabricCompilationImpl> = project.lazyConfigurable {
        val test = project.objects.newInstance<FabricCompilationImpl>(
            FabricCompilationInfo(
                ClochePlugin.CLIENT_TEST_COMPILATION_NAME,
                target,
                info.intermediaryMinecraftClasspath,
                info.splitNamedGames,
                info.runtimeExtraMinecraftFiles,
                info.extraClasspathFiles,
                data = false,
                test = true,
                primaryGame = project.provider { GamePart.CLIENT },
                preapplicationSide = info.preapplicationSide,
                preapplicationLibraries = info.preapplicationLibraries,
            ),
        )

        target.test.onConfigured {
            it.generateMetadataTask.configure {
                clientMixinConfigs.from(test.mixins)
            }
        }

        test
    }
}
