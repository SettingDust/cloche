import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class GameJarProbe {
    public static void premain(String expectation) throws Exception {
        for (var arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            System.out.println("GAME_JARS_ARG " + arg);
        }
        System.out.println("GAME_JARS_CP " + System.getProperty("java.class.path"));
        String path = System.getProperty("connector.clean.path");
        System.out.println("GAME_JARS_PATH " + path);
        if (!("absent".equals(expectation) && path == null || "required".equals(expectation) && path != null)) {
            throw new AssertionError("Expected connector.clean.path " + expectation + ", got " + path);
        }
        if (path != null) {
            Path probe = Path.of(GameJarProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path expected = Path.of(Files.readString(probe.resolveSibling("expected-patched.txt")).trim());
            if (!Path.of(path).isAbsolute() || !Files.isSameFile(expected, Path.of(path))) {
                throw new AssertionError("Expected " + expected + ", got " + path);
            }
            try (var jar = new JarFile(path)) {
                var forge = jar.getJarEntry("net/minecraftforge/common/MinecraftForge.class");
                var game = jar.getJarEntry("net/minecraft/world/entity/Entity.class");
                if (forge == null || game == null) throw new AssertionError("Missing Forge or Entity class");
                var out = new java.io.StringWriter();
                var javap = java.util.spi.ToolProvider.findFirst("javap").orElseThrow();
                int result = javap.run(new java.io.PrintWriter(out), new java.io.PrintWriter(out),
                    "-classpath", path, "-p", "net.minecraft.world.entity.Entity");
                if (result != 0 || !out.toString().contains("void m_8119_();")) {
                    throw new AssertionError("Missing SRG Entity.tick() declaration");
                }
                System.out.println("GAME_JARS_VERIFIED " + expected);
            }
        }
        System.exit(0);
    }
}
