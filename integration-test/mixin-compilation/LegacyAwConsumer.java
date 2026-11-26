import earth.terrarium.cloche.target.compilation.FinalJarTasks;
import earth.terrarium.cloche.target.compilation.TargetCompilation;
import earth.terrarium.cloche.target.compilation.TargetCompilationInfo;
import earth.terrarium.cloche.target.compilation.TargetCompilationKt;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

/** Compiled before the AW extraction and executed against its retained result. */
public class LegacyAwConsumer {
    public static FinalJarTasks result(TargetCompilation<?> compilation) throws Exception {
        var getter = TargetCompilation.class.getDeclaredMethod("getSetupFiles");
        getter.setAccessible(true);
        var result = (FinalJarTasks) getter.invoke(compilation);
        if (result.component1() != result.getAccessWidenTask()
            || result.component2() != result.getDecompileTask()
            || result.component3() != result.getRuntimeExtraFiles()
            || result.component4() != result.getCompileMinecraftFiles()
            || result.component5() != result.getPreapplyMixinsTasks()) throw new AssertionError("Legacy fields");
        if (!result.getLibraryArtifact().get().equals(compilation.getFinalMinecraftFile().get())
            || !result.getSourcesArtifact().get().equals(compilation.getSources().get())) throw new AssertionError("Legacy outputs");
        TargetCompilationKt.class.getMethod("registerCompilationTransformations", TargetCompilation.class, TargetCompilationInfo.class);
        return result;
    }
}
