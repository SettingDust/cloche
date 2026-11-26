import org.gradle.tooling.*;
import org.gradle.tooling.model.build.BuildEnvironment;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Produce sync inputs and invoke every real IDEA builder in the same Tooling request. */
public class R4Tooling {
    public interface Observation { String getJson(); }
    public static void main(String[] args) throws Exception {
        File root = new File(args[0]);
        Properties expected = new Properties();
        try (InputStream input = new FileInputStream(new File(root, "environment.properties"))) {
            expected.load(input);
        }
        File tooling = Paths.get(GradleConnector.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath().toFile();
        File lib = tooling.getParentFile();
        if (!tooling.isFile() || !lib.getName().equals("lib")) {
            throw new IllegalStateException("Cannot locate Gradle installation from loaded Tooling API: " + tooling);
        }
        File installation = lib.getParentFile();
        File javaHome = Paths.get(System.getProperty("java.home")).toRealPath().toFile();
        if (!javaHome.equals(Paths.get(expected.getProperty("workerJavaHome")).toRealPath().toFile()) ||
                !installation.equals(Paths.get(expected.getProperty("gradleHome")).toRealPath().toFile())) {
            throw new IllegalStateException("Helper differs from worker sources: " + javaHome + " " + tooling);
        }
        System.out.println("HELPER_ENV JavaHome=" + javaHome + " ToolingAPI=" + tooling + " Installation=" + installation);
        List<String> arguments = new ArrayList<>(Arrays.asList(
            "--init-script", new File(root, "observe.init.gradle").getPath(),
            "--offline", "--info", "--stacktrace", "--console=plain", "--max-workers=2",
            "--no-configuration-cache", "-Didea.resolveSourceSetDependencies=true",
            "-PclocheClasspathMetadata=" + new File(root, "classpath.properties").getPath(),
            "-Pscenario=" + args[1]));
        arguments.addAll(Arrays.asList(args).subList(2, args.length));
        try (ProjectConnection connection = GradleConnector.newConnector()
                .useInstallation(installation).useGradleUserHomeDir(new File(root, "private-home"))
                .forProjectDirectory(root).connect()) {
            BuildEnvironment environment = connection.getModel(BuildEnvironment.class);
            File buildJava = environment.getJava().getJavaHome().toPath().toRealPath().toFile();
            File buildHome = environment.getGradle().getGradleUserHome().getCanonicalFile();
            System.out.println("BUILD_ENV Gradle=" + environment.getGradle().getGradleVersion() + " JavaHome=" + buildJava + " Home=" + buildHome);
            if (!javaHome.equals(buildJava) || !buildHome.equals(new File(expected.getProperty("privateHome")).getCanonicalFile()) ||
                    !environment.getGradle().getGradleVersion().equals(expected.getProperty("gradleVersion"))) {
                throw new IllegalStateException("BuildEnvironment differs from worker/private Home: " + buildJava + " " + buildHome);
            }
            try (OutputStream log = new FileOutputStream(new File(root, "model.log"))) {
                Observation model = connection.model(Observation.class).forTasks(":clocheIdeSync")
                    .withArguments(arguments).setStandardOutput(log).setStandardError(log).get();
                Files.writeString(new File(root, "model.json").toPath(), model.getJson());
            }
        }
    }
}
