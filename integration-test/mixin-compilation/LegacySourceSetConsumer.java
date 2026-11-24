import earth.terrarium.cloche.api.target.ClocheTarget;
import earth.terrarium.cloche.target.compilation.CompilationInternal;
import earth.terrarium.cloche.target.compilation.CompilationInternalKt;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

public final class LegacySourceSetConsumer {
    public static void configure(Project project, SourceSet sourceSet, ClocheTarget target, CompilationInternal compilation) {
        CompilationInternalKt.configureSourceSet(project, sourceSet, target, compilation);
    }
}
