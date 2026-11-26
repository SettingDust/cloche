package earth.terrarium.cloche

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider

/** Small real Cloche consumer. Only Minecraft download inputs are replaced by tiny local games. */
class MixinCompilationTests {
    @Test
    fun sourcesBoundaryKeepsLazyHandlesAndExplicitPreparation() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        val directory = Files.createTempDirectory(invocation, "sources-boundary-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "sides")
        Files.copy(
            Path.of("integration-test/mixin-compilation/sources-boundary.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        println("I_SOURCES_BOUNDARY $directory")
        try {
            val result =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifySourcesBoundary",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    ).build()
            Files.writeString(directory.resolve("boundary.log"), result.output)
            assertTrue(result.output.contains("VERIFIED_SOURCES_BOUNDARY_LAZY_EXPLICIT_HANDLES_PREPARATION"), result.output)
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("boundary-failure.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun sourcesKeepColdSelectionOwnAwAndNativePreparation() {
        for ((form, enabled) in listOf("included" to true, "split" to true, "split" to false, "joined" to true, "joined" to false)) {
            val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
            val directory = Files.createTempDirectory(invocation, "sources-$form-$enabled-")
            val home = directory.resolve("private-home")
            prepare(directory, home, if (enabled) "on" else "off")
            Files.copy(
                Path.of("integration-test/mixin-compilation/sources-selection.gradle"),
                directory.resolve("sources-selection.gradle"),
            )
            if (form == "joined") {
                val mappings = directory.resolve("input/mappings.tiny")
                Files.writeString(mappings, Files.readString(mappings).replace("intermediary", "srg"))
                val build = Files.readString(Path.of("integration-test/mixin-compilation/joined-consumption.gradle"))
                Files.writeString(
                    directory.resolve("build.gradle"),
                    build.replace(
                        "def c = target.main\n",
                        "def c = target.main\napply from: 'sources-selection.gradle'\n",
                    ),
                )
            } else {
                Files.writeString(
                    directory.resolve("build.gradle"),
                    "\napply from: 'sources-selection.gradle'\n",
                    java.nio.file.StandardOpenOption.APPEND,
                )
            }
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            val originals =
                Files.walk(directory.resolve("repository")).use { paths ->
                    paths.filter { it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
                }
            println("I_SOURCES_SELECTION $form $enabled $directory")

            fun runner(vararg extra: String) =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            "verifySourceSelection",
                            "--offline",
                            "--info",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PsourceForm=$form",
                            "-Pscenario=${if (enabled) "on" else "off"}",
                            "-PjoinedLoader=forge",
                            "-PjoinedEnabled=$enabled",
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        ) +
                            extra,
                    )
            try {
                val result = runner().build()
                Files.writeString(directory.resolve("consumer.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_SOURCE_SELECTION $form $enabled"), result.output)
                for (compiler in listOf(":compileJava", ":compileKotlin")) {
                    result.task(compiler)?.let { assertEquals(org.gradle.testkit.runner.TaskOutcome.NO_SOURCE, it.outcome) }
                }
                assertNotNull(result.task(":produceSourcesLibrary"))
                if (!enabled) assertFalse(result.tasks.any { it.path.startsWith(":preapply") })
                if (form == "included") {
                    val sources = directory.resolve("build/minecraft/main/minecraft-1.20.1-client-named-sources.jar")
                    val oldBytes = Files.readAllBytes(sources)
                    for ((fault, message) in listOf(
                        "aw" to "sources_aw_failure",
                        "library" to "sources_library_failure",
                        "strip" to "broken source-only record",
                    )) {
                        val failure = runner("-PsourceFault=$fault", "--rerun-tasks").buildAndFail()
                        Files.writeString(directory.resolve("failure-$fault.log"), failure.output)
                        assertTrue(
                            failure.output.contains(message) || fault == "strip" && failure.output.contains("StripMixins"),
                            failure.output,
                        )
                        if (fault == "strip") {
                            assertEquals(org.gradle.testkit.runner.TaskOutcome.FAILED, failure.task(":decompileMinecraft")?.outcome)
                            assertFalse(failure.output.contains("SOURCES_SELECTION_READY"), failure.output)
                        } else {
                            assertNull(failure.task(":decompileMinecraft"))
                        }
                        assertNull(failure.task(":verifySourceSelection"))
                        assertArrayEquals(oldBytes, Files.readAllBytes(sources))
                        val recovery = runner().build()
                        Files.writeString(directory.resolve("recovery-$fault.log"), recovery.output)
                        assertTrue(recovery.output.contains("VERIFIED_SOURCE_SELECTION included true"), recovery.output)
                    }
                }
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("consumer-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun previewBoundaryKeepsShapesLateInputsAndPairedRecords() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        val directory = Files.createTempDirectory(invocation, "preview-boundary-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "sides")
        Files.copy(
            Path.of("integration-test/mixin-compilation/preview-boundary.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        println("I_PREVIEW_BOUNDARY $directory")
        try {
            val result =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyPreviewBoundary",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    ).build()
            Files.writeString(directory.resolve("boundary.log"), result.output)
            assertTrue(
                result.output.contains("VERIFIED_PREVIEW_BOUNDARY_SHAPES_LATE_SIDE_ENABLED_RECORDS_PRODUCERS_EMPTY_NAMESPACE"),
                result.output,
            )
            assertNotNull(result.task(":verifyPreviewBoundary"))
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("boundary-failure.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun previewConsumersKeepRealFilesAndPairedRecords() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        // on/off preview assertions share the classifier success graph below.
        for (scenario in listOf("file", "direct")) {
            val directory = Files.createTempDirectory(invocation, "preview-$scenario-")
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            seedCompilerDependencies(home)
            for (name in listOf("decompile.gradle", "preview-contract.gradle")) {
                Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
                Files.writeString(directory.resolve("build.gradle"), "\napply from: '$name'\n", java.nio.file.StandardOpenOption.APPEND)
            }
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            val originals =
                Files.walk(directory).use { paths ->
                    paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
                }
            println("I_PREVIEW_CONSUMER $scenario $directory")
            try {
                // Keep the compiler's first production independent of sources and other consumers.
                val compile =
                    GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile())
                        .withArguments(
                            "verifyCompile", "--offline", "--console=plain", "--stacktrace", "--max-workers=2",
                            "--no-configuration-cache", "-g", home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}", "-Pscenario=$scenario",
                        ).build()
                Files.writeString(directory.resolve("compile-first.log"), compile.output)
                assertTrue(compile.output.contains("VERIFIED_COMPILE $scenario"), compile.output)
                for (task in listOf(":compileJava", ":compileKotlin")) assertNotNull(compile.task(task))
                assertNull(compile.task(":decompileMinecraft"))
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
                val result =
                    GradleRunner
                        .create()
                        .withProjectDir(directory.toFile())
                        .withTestKitDir(home.toFile())
                        .withArguments(
                            "verifyPreviewContract",
                            "--offline",
                            "--info",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            "-Pscenario=$scenario",
                        ).build()
                Files.writeString(directory.resolve("consumer.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_COMPILE $scenario"), result.output)
                assertTrue(result.output.contains("VERIFIED_PREVIEW_PAIRED_RECORDS_REAL_CONSUMPTION"), result.output)
                for (task in listOf(":compileJava", ":compileKotlin", ":decompileMinecraft")) assertNotNull(result.task(task))
                if (scenario == "off") {
                    assertFalse(result.tasks.any { it.path.startsWith(":preapply") })
                } else {
                    for (task in listOf(":preapplyCommonMixins", ":preapplyClientMixins")) assertNotNull(result.task(task))
                }
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("consumer-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun accessWideningKeepsShapesLazyPrimaryTasksAndConditionalHelpers() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        val directory = Files.createTempDirectory(invocation, "aw-boundary-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "sides")
        Files.copy(
            Path.of("integration-test/mixin-compilation/aw-boundary.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        println("I_AW_BOUNDARY $directory")
        try {
            val result =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyAwBoundary",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    ).build()
            Files.writeString(directory.resolve("boundary.log"), result.output)
            assertTrue(result.output.contains("VERIFIED_AW_BOUNDARY_LAZY_ROLES_IDENTITIES_PRODUCERS_NAMESPACE_FAILURES"), result.output)
            assertNotNull(result.task(":verifyAwBoundary"))
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("boundary-failure.log"), failure.buildResult.output)
            throw failure
        }
    }

    private fun prepareLegacyAwConsumer(directory: Path) {
        val legacy = Files.createDirectories(directory.resolve("legacy-aw-classes"))
        val baseline = System.getProperty("fixture.legacyAwClasses")
        if (baseline != null) {
            Files.copy(Path.of(baseline, "LegacyAwConsumer.class"), legacy.resolve("LegacyAwConsumer.class"))
        } else {
            val metadata = Properties().apply { File(System.getProperty("fixture.metadata")).inputStream().use(::load) }
            val gradleApi =
                File(
                    org.gradle.api.Project::class.java.protectionDomain.codeSource.location
                        .toURI(),
                )
            assertEquals(
                0,
                ToolProvider.getSystemJavaCompiler().run(
                    null,
                    null,
                    null,
                    "--release",
                    "17",
                    "-proc:none",
                    "-classpath",
                    metadata.getProperty("implementation-classpath") + File.pathSeparator + gradleApi.path,
                    "-d",
                    legacy.toString(),
                    "integration-test/mixin-compilation/LegacyAwConsumer.java",
                ),
            )
        }
    }

    @Test
    fun sourceSetBoundariesRetainDirectoriesClassifiersAndTiming() {
        for (early in listOf(true, false)) {
            for (single in listOf(false, true)) {
                val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
                val directory = Files.createTempDirectory(invocation, "sourceset-$early-$single-")
                val home = directory.resolve("private-home")
                prepare(directory, home, "sourceset")
                seedCompilerDependencies(home)
                val legacy = Files.createDirectories(directory.resolve("legacy-sourceset-classes"))
                val baseline = System.getProperty("fixture.legacySourceSetClasses")
                if (baseline != null) {
                    Files.copy(Path.of(baseline, "LegacySourceSetConsumer.class"), legacy.resolve("LegacySourceSetConsumer.class"))
                } else {
                    val metadata = Properties().apply { File(System.getProperty("fixture.metadata")).inputStream().use(::load) }
                    val gradleApi =
                        File(
                            org.gradle.api.Project::class.java.protectionDomain.codeSource.location
                                .toURI(),
                        )
                    assertEquals(
                        0,
                        ToolProvider.getSystemJavaCompiler().run(
                            null,
                            null,
                            null,
                            "--release",
                            "17",
                            "-proc:none",
                            "-classpath",
                            metadata.getProperty("implementation-classpath") + File.pathSeparator + gradleApi.path,
                            "-d",
                            legacy.toString(),
                            "integration-test/mixin-compilation/LegacySourceSetConsumer.java",
                        ),
                    )
                }
                Files.copy(
                    Path.of("integration-test/mixin-compilation/sourceset.gradle"),
                    directory.resolve("build.gradle"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
                println("I_SOURCESET early=$early single=$single $directory")
                try {
                    val result =
                        GradleRunner
                            .create()
                            .withProjectDir(directory.toFile())
                            .withTestKitDir(home.toFile())
                            .withArguments(
                                "verifySourceSetBoundaries",
                                "--offline",
                                "--console=plain",
                                "--stacktrace",
                                "--max-workers=2",
                                "--no-configuration-cache",
                                "-g",
                                home.toString(),
                                "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                                "-PearlyKotlin=$early",
                                "-PsingleTarget=$single",
                            ).build()
                    Files.writeString(directory.resolve("gradle.log"), result.output)
                    assertTrue(
                        result.output.contains("VERIFIED_SOURCESET_DIRECTORIES_SYNC_CLASSIFIERS_IDENTITIES_TIMING_LEGACY"),
                        result.output,
                    )
                    assertNotNull(result.task(":verifySourceSetBoundaries"))
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("gradle-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun dependencyViewsKeepFullRawInputsBucketsAndNativeProducers() {
        for (scenario in listOf("on", "off", "file")) {
            val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "i-views-$scenario-")
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario, classifier = true)
            seedCompilerDependencies(home)
            Files.copy(Path.of("integration-test/mixin-compilation/views.gradle"), directory.resolve("views.gradle"))
            Files.writeString(directory.resolve("build.gradle"), "\napply from: 'views.gradle'\n", java.nio.file.StandardOpenOption.APPEND)
            Files.writeString(directory.resolve("src/main/java/Consume.java"), "public class Consume { named.Game game; }\n")
            Files.writeString(directory.resolve("src/main/kotlin/ConsumeKotlin.kt"), "class ConsumeKotlin(val game: named.Game)\n")
            println("I_VIEWS $scenario $directory")
            for ((task, marker) in listOf(
                "verifyRawViews" to "VERIFIED_RAW_VIEWS_IDENTITY_BUCKETS_LAZY_PRODUCERS",
                "verifyViewsClasspath" to "VERIFIED_VIEWS_CLASSPATH_USER_ADDITIONS_NATIVE_PROJECTS",
            )) {
                try {
                    val result =
                        GradleRunner
                            .create()
                            .withProjectDir(directory.toFile())
                            .withTestKitDir(home.toFile())
                            .withArguments(
                                task,
                                "--offline",
                                "--console=plain",
                                "--stacktrace",
                                "--max-workers=2",
                                "--no-configuration-cache",
                                "-g",
                                home.toString(),
                                "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                                "-Pscenario=$scenario",
                                "-PclassifierProbe=true",
                            ).build()
                    Files.writeString(directory.resolve("$task.log"), result.output)
                    assertTrue(result.output.contains(marker), result.output)
                    assertNotNull(result.task(":$task"))
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("$task-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun composedSetupKeepsIndependentLatePrimarySideAndEnabled() {
        for ((primary, side, enabled) in listOf(
            Triple("COMMON", "SERVER", true),
            Triple("COMMON", "CLIENT", true),
            Triple("CLIENT", "CLIENT", true),
            Triple("CLIENT", "CLIENT", false),
        )) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "i-compose-late-$primary-$side-$enabled-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, "direct")
            Files.copy(Path.of("integration-test/mixin-compilation/compose-late.gradle"), directory.resolve("compose-late.gradle"))
            Files.writeString(
                directory.resolve("build.gradle"),
                "\napply from: 'compose-late.gradle'\n",
                java.nio.file.StandardOpenOption.APPEND,
            )
            println("I_COMPOSE_LATE $primary $side $enabled $directory")
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyComposeLateInputs",
                        "--offline",
                        "--info",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        "-Pscenario=direct",
                        "-PcomposePrimary=$primary",
                        "-PcomposeSide=$side",
                        "-PcomposeEnabled=$enabled",
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("compose-late.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_COMPOSE_LATE $primary $side $enabled"), result.output)
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun composedPreparationCoversColdNativeConsumersAndBlocksFailures() {
        val cases =
            listOf(
                Triple("configuration", true, ""),
                Triple("view", true, ""),
                Triple("java", true, "record"),
                Triple("kotlin", true, "record"),
                Triple("sources", true, "native"),
                Triple("view", false, ""),
                Triple("sources", false, ""),
            )
        for ((consumer, enabled, fault) in cases) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "i-compose-$consumer-$enabled-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, "direct")
            if (consumer in listOf("java", "kotlin")) seedCompilerDependencies(home)
            Files.copy(Path.of("integration-test/mixin-compilation/compose.gradle"), directory.resolve("compose.gradle"))
            Files.writeString(
                directory.resolve("build.gradle"),
                "\napply from: 'compose.gradle'\n",
                java.nio.file.StandardOpenOption.APPEND,
            )
            println("I_COMPOSE_COLD $consumer $enabled $fault $directory")
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            val originals =
                Files.walk(directory).use { paths ->
                    paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
                }

            fun runner(vararg extra: String): GradleRunner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            "verifyComposedConsumer",
                            "--offline",
                            "--info",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            "-Pscenario=direct",
                            "-PcomposeConsumer=$consumer",
                            "-PcomposeEnabled=$enabled",
                        ) + extra,
                    )
            try {
                if (fault.isNotEmpty()) {
                    val failed = runner("-PcomposeFault=$fault").buildAndFail()
                    Files.writeString(directory.resolve("expected-$fault-failure.log"), failed.output)
                    assertTrue(failed.output.contains(if (fault == "native") "compose_native_failure" else "Cannot strip"), failed.output)
                    assertFalse(
                        failed.output.contains("COMPOSE_CONSUMER_READY") || failed.output.contains("VERIFIED_COMPOSED_CONSUMPTION"),
                        failed.output,
                    )
                    originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
                }
                val result = (if (fault == "record") runner("--rerun-tasks") else runner()).build()
                Files.writeString(directory.resolve("compose-success.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_COMPOSED_CONSUMPTION $consumer $enabled"), result.output)
                assertTrue(Files.isRegularFile(directory.resolve("compose-observation.json")))
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun classifierArtifactsRetainExactRolesAndConsumers() {
        for (scenario in listOf("on", "off")) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "ct3-classifier-$scenario-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario, classifier = true)
            seedCompilerDependencies(home)
            for (name in listOf("decompile.gradle", "classifier.gradle", "preview-contract.gradle")) {
                Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
            }
            Files.writeString(
                directory.resolve("build.gradle"),
                "\napply from: 'decompile.gradle'\napply from: 'classifier.gradle'\napply from: 'preview-contract.gradle'\n",
                java.nio.file.StandardOpenOption.APPEND,
            )
            println("CT3_CLASSIFIER $scenario $directory")
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            val originals =
                Files.walk(directory).use { paths ->
                    paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
                }

            fun runner(
                task: String,
                vararg extra: String,
            ): GradleRunner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            task,
                            *if (task == "verifyClassifier") arrayOf("verifyPreviewContract") else emptyArray(),
                            "--offline",
                            "--info",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            "-Pscenario=$scenario",
                            "-PclassifierProbe=true",
                        ) +
                            extra,
                    )
            try {
                // First production is compiler-only; later normal consumers reuse this same graph.
                val compile = runner("verifyCompile").build()
                Files.writeString(directory.resolve("compile-first.log"), compile.output)
                assertTrue(compile.output.contains("VERIFIED_COMPILE $scenario"), compile.output)
                for (task in listOf(":compileJava", ":compileKotlin")) assertNotNull(compile.task(task))
                assertNull(compile.task(":decompileMinecraft"))
                assertNull(compile.task(":clocheIdeSync"))
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
                val success = runner("verifyClassifier").build()
                Files.writeString(directory.resolve("classifier.log"), success.output)
                assertTrue(success.output.contains("VERIFIED_CLASSIFIER_ROLES_EXTRA_API_RUNTIME_SCOPE"), success.output)
                assertTrue(success.output.contains("VERIFIED_PREVIEW_PAIRED_RECORDS_REAL_CONSUMPTION"), success.output)
                assertTrue(success.output.contains("VERIFIED_COMPILE $scenario"), success.output)
                assertNotNull(success.task(":decompileMinecraft"))
                if (scenario != "off") {
                    for (task in listOf(":preapplyCommonMixins", ":preapplyClientMixins")) assertNotNull(success.task(task))
                }
                assertNotNull(success.task(":compileJava"))
                assertNotNull(success.task(":compileKotlin"))
                val negative = runner("compileRuntimeNegative").buildAndFail()
                Files.writeString(directory.resolve("runtime-negative.log"), negative.output)
                assertTrue(negative.output.contains("callRuntime()"), negative.output)
                assertEquals(org.gradle.testkit.runner.TaskOutcome.FAILED, negative.task(":compileRuntimeNegative")?.outcome)
                val kotlinNegative = directory.resolve("src/main/kotlin/RuntimeNegative.kt")
                Files.writeString(kotlinNegative, "fun runtimeNegative(g: named.Game): Int = g.callRuntime()\n")
                try {
                    val rejected = runner("compileKotlin").buildAndFail()
                    Files.writeString(directory.resolve("runtime-negative-kotlin.log"), rejected.output)
                    assertTrue(rejected.output.contains("callRuntime"), rejected.output)
                    assertEquals(org.gradle.testkit.runner.TaskOutcome.FAILED, rejected.task(":compileKotlin")?.outcome)
                } finally {
                    Files.delete(kotlinNegative)
                }
                if (scenario == "on") {
                    val oldSources = directory.resolve("build/minecraft/main/minecraft-1.20.1-named-sources.jar")
                    val oldBytes = Files.readAllBytes(oldSources)
                    for (consumer in listOf("source", "game")) {
                        for (step in listOf("first", "second")) {
                            val task = if (consumer == "source") "verifyC" else "verifyGameOnly"
                            val fault = runner(task, "-Pct3Fault=$consumer:$step").buildAndFail()
                            Files.writeString(directory.resolve("$consumer-$step-failure.log"), fault.output)
                            assertTrue(fault.output.contains("Cannot strip"), fault.output)
                            assertTrue(
                                fault.output.replace('\\', '/').contains(
                                    if (step ==
                                        "first"
                                    ) {
                                        "main/common/applied-mixins.json"
                                    } else {
                                        "main/client/applied-mixins.json"
                                    },
                                ),
                                fault.output,
                            )
                            val diagnostic = fault.output.lines().first { it.contains("Cannot strip") }
                            assertEquals(step == "second", diagnostic.substringAfter(" from ").contains(".codev-strip-"), diagnostic)
                            assertTrue(diagnostic.contains("record ${if (step == "first") 1 else 2}"), diagnostic)
                            assertTrue(fault.output.contains("Caused by:"), fault.output)
                            assertFalse(fault.output.contains("VERIFIED_PROBE") || fault.output.contains("VERIFIED_GAME_ONLY"))
                            assertArrayEquals(oldBytes, Files.readAllBytes(oldSources))
                        }
                    }
                    val recovery = runner("verifyClassifier").build()
                    Files.writeString(directory.resolve("recovery.log"), recovery.output)
                    assertTrue(recovery.output.contains("VERIFIED_CLASSIFIER_ROLES_EXTRA_API_RUNTIME_SCOPE true"), recovery.output)
                    val gameRecovery = runner("verifyGameOnly").build()
                    Files.writeString(directory.resolve("game-recovery.log"), gameRecovery.output)
                    assertTrue(gameRecovery.output.contains("VERIFIED_GAME_ONLY"), gameRecovery.output)
                } else {
                    assertFalse(success.tasks.any { it.path.startsWith(":preapply") })
                    assertFalse(success.output.contains("Caching disabled for SelectMixinSources:"))
                }
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun sourcesConsumeRealCleanClientAndRestoreOldView() {
        for (scenario in listOf("on", "off", "file")) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "r2-sources-$scenario-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            Files.copy(Path.of("integration-test/mixin-compilation/decompile.gradle"), directory.resolve("decompile.gradle"))
            Files.writeString(
                directory.resolve("build.gradle"),
                "\napply from: 'decompile.gradle'\n",
                java.nio.file.StandardOpenOption.APPEND,
            )
            println("R2_SOURCES $scenario $directory")

            fun runner(vararg extra: String): GradleRunner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            "verifySources",
                            "--info",
                            "--offline",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            "-Pscenario=$scenario",
                        ) +
                            extra,
                    )
            try {
                val result = runner().build()
                Files.writeString(directory.resolve("gradle.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_SOURCES $scenario"), result.output)
                assertNotNull(result.task(":decompileMinecraft"))
                assertNull(result.task(":compileJava"))
                assertNull(result.task(":compileKotlin"))
                if (scenario == "off") {
                    assertFalse(result.tasks.any { it.path.startsWith(":preapply") })
                    assertFalse(result.output.contains("Caching disabled for SelectMixinSources:"))
                    assertFalse(result.output.contains("Caching disabled for StripMixins:"))
                } else {
                    assertNotNull(result.task(":preapplyCommonMixins"))
                    assertNotNull(result.task(":preapplyClientMixins"))
                    val production =
                        maxOf(result.output.indexOf("> Task :preapplyCommonMixins"), result.output.indexOf("> Task :preapplyClientMixins"))
                    val selection = result.output.indexOf("Caching disabled for SelectMixinSources:")
                    val gameStrip = result.output.indexOf("Caching disabled for StripMixins:")
                    assertTrue(production >= 0 && selection > production && gameStrip > production)
                    assertTrue(production < result.output.indexOf("R2_DECOMPILE_INPUT_READY"))
                    assertTrue(result.output.contains("LOCAL_SOURCE_MATCH true"))
                }
                if (scenario == "on") {
                    // Keep successful old sources present: an engine failure must still fail the public consumer.
                    val oldSources = directory.resolve("build/minecraft/main/minecraft-1.20.1-named-sources.jar")
                    val oldBytes = Files.readAllBytes(oldSources)
                    val failure = runner("-Pr2Fault=true").buildAndFail()
                    Files.writeString(directory.resolve("failure.log"), failure.output)
                    assertEquals(org.gradle.testkit.runner.TaskOutcome.FAILED, failure.task(":preapplyCommonMixins")?.outcome)
                    assertTrue(failure.output.contains("r2_missing_namespace"), failure.output)
                    assertNull(failure.task(":decompileMinecraft"))
                    assertNull(failure.task(":verifySources"))
                    assertFalse(failure.output.contains("VERIFIED_SOURCES"))
                    assertArrayEquals(oldBytes, Files.readAllBytes(oldSources))
                    val recovery = runner().build()
                    Files.writeString(directory.resolve("recovery.log"), recovery.output)
                    assertTrue(recovery.output.contains("VERIFIED_SOURCES on"), recovery.output)
                }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun isolatedNativeCompileViewsProduceNotificationBeforeCleanup() {
        for ((scenario, view) in listOf("on", "file").flatMap { scenario -> listOf("C", "View", "SourceSet").map { scenario to it } }) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "r1-probe-$scenario-$view-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            println("R1_PROBE $scenario $view $directory")
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verify$view",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        "-Pscenario=$scenario",
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("gradle.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_PROBE $view"), result.output)
                assertNotNull(result.task(":preapplyCommonMixins"))
                assertNotNull(result.task(":preapplyClientMixins"))
                assertNull(result.task(":compileJava"))
                assertNull(result.task(":compileKotlin"))
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun joinedLegacyClasspathKeepsOriginalAwFiles() {
        for (off in listOf(false, true)) {
            val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "c-legacy-$off-")
            val home = directory.resolve("private-home")
            prepare(directory, home, "sides")
            val mappings = directory.resolve("input/mappings.tiny")
            Files.writeString(mappings, Files.readString(mappings).replace("intermediary", "srg"))
            Files.copy(
                Path.of("integration-test/mixin-compilation/sides.gradle"),
                directory.resolve("build.gradle"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            for (name in listOf("final-legacy.gradle", "final-legacy-real.gradle")) {
                Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
                Files.writeString(directory.resolve("build.gradle"), "\napply from: '$name'\n", java.nio.file.StandardOpenOption.APPEND)
            }
            println("C_LEGACY $off $directory")
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            "verifyR5LegacyFiles",
                            "--offline",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        ) +
                            if (off) listOf("-Pr5ForgeOff=true") else emptyList(),
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("legacy.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_R5_LEGACY_FILES 3"), result.output)
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("legacy-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun platformInputsKeepPreviewAwAndRuntimeSourcesDistinct() {
        for ((form, scenario) in listOf("included" to "on", "split" to "on", "unsplit" to "on", "included" to "off")) {
            val directory =
                Files.createTempDirectory(
                    Path.of(System.getProperty("fixture.metadata")).parent.parent,
                    "c-platform-$form-$scenario-",
                )
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            seedCompilerDependencies(home)
            prepareLegacyAwConsumer(directory)
            for (name in listOf("platform.gradle", "aw-legacy.gradle")) {
                Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
                Files.writeString(directory.resolve("build.gradle"), "\napply from: '$name'\n", java.nio.file.StandardOpenOption.APPEND)
            }
            println("C_PLATFORM $form $scenario $directory")
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyPlatform",
                        "--offline",
                        "--info",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        "-Pscenario=$scenario",
                        "-PplatformForm=$form",
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("platform.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_PLATFORM $form ${scenario == "on"}"), result.output)
                assertTrue(result.output.contains("PLATFORM_AW_PRODUCED"), result.output)
                assertTrue(result.output.contains("PLATFORM_SOURCES_READY"), result.output)
                assertTrue(result.output.contains("VERIFIED_AW_LEGACY_RESULT_NO_ADDITIONAL_REGISTRATION"), result.output)
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("platform-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun fabricAssemblyPreservesExistingLateAndTypedInputs() {
        val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "i-fabric-assembly-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "sides")
        Files.copy(
            Path.of("integration-test/mixin-compilation/fabric-assembly.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        println("I_FABRIC_ASSEMBLY $directory")
        val runner =
            GradleRunner
                .create()
                .withProjectDir(directory.toFile())
                .withTestKitDir(home.toFile())
                .withArguments(
                    "verifyFabricAssembly",
                    "--offline",
                    "--console=plain",
                    "--stacktrace",
                    "--max-workers=2",
                    "--no-configuration-cache",
                    "-g",
                    home.toString(),
                    "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                )
        try {
            val result = runner.build()
            Files.writeString(directory.resolve("assembly.log"), result.output)
            assertTrue(result.output.contains("VERIFIED_FABRIC_ASSEMBLY"), result.output)
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("assembly-failure.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun joinedAssemblyPreservesLazyRawAndTypedInputs() {
        val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "i-joined-assembly-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "sides")
        Files.copy(
            Path.of("integration-test/mixin-compilation/joined-assembly.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        println("I_JOINED_ASSEMBLY $directory")
        try {
            val result =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyJoinedAssembly",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    ).build()
            Files.writeString(directory.resolve("assembly.log"), result.output)
            assertTrue(result.output.contains("VERIFIED_JOINED_ASSEMBLY"), result.output)
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("assembly-failure.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun joinedConsumersProduceAndUseSingleClientShard() {
        for (loader in listOf("forge", "neoforge")) {
            for (enabled in listOf(true, false)) {
                val directory =
                    Files.createTempDirectory(
                        Path.of(System.getProperty("fixture.metadata")).parent.parent,
                        "i-joined-consumer-$loader-$enabled-",
                    )
                val home = directory.resolve("private-home")
                prepare(directory, home, if (loader == "forge") "sides" else "direct")
                if (loader == "forge") {
                    val mappings = directory.resolve("input/mappings.tiny")
                    Files.writeString(mappings, Files.readString(mappings).replace("intermediary", "srg"))
                }
                Files.writeString(
                    directory.resolve("JoinedConsume.java"),
                    if (enabled) {
                        "public class JoinedConsume { int use(named.Game g) { probe.Ref ref = g; return ref.callRef(); } }"
                    } else {
                        "public class JoinedConsume { named.Game game; }"
                    },
                )
                Files.copy(
                    Path.of("integration-test/mixin-compilation/joined-consumption.gradle"),
                    directory.resolve("build.gradle"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
                println("I_JOINED_CONSUMPTION $loader $enabled $directory")
                try {
                    val result =
                        GradleRunner
                            .create()
                            .withProjectDir(directory.toFile())
                            .withTestKitDir(home.toFile())
                            .withArguments(
                                "verifyJoinedConsumption",
                                "--offline",
                                "--console=plain",
                                "--stacktrace",
                                "--max-workers=2",
                                "--no-configuration-cache",
                                "-g",
                                home.toString(),
                                "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                                "-PjoinedLoader=$loader",
                                "-PjoinedEnabled=$enabled",
                            ).build()
                    Files.writeString(directory.resolve("consumption.log"), result.output)
                    assertTrue(result.output.contains("VERIFIED_JOINED_CONSUMPTION $loader $enabled"), result.output)
                    assertTrue(result.output.contains("JOINED_RAW_PRODUCED"), result.output)
                    assertTrue(result.output.contains("JOINED_EXTRA_PRODUCED"), result.output)
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("consumption-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun configuredCompilationsInheritEnvironmentAndTargetSwitch() {
        for (splitEnabled in listOf(false, true)) {
            val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "r2-sides-$splitEnabled-")
            val home = directory.resolve("private-home")
            prepare(directory, home, "sides")
            Files.copy(
                Path.of("integration-test/mixin-compilation/sides.gradle"),
                directory.resolve("build.gradle"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            println("R2_SIDES splitEnabled=$splitEnabled $directory")
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        listOf(
                            "verifySides",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        ) +
                            if (splitEnabled) listOf("-Pr2SplitOn=true") else emptyList(),
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("gradle.log"), result.output)
                assertTrue(result.output.contains("VERIFIED_SIDES 14"), result.output)
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun localCompilationIdentitiesRemainLazyAndNative() {
        for (unnamed in listOf(false, true)) {
            for (clientFirst in listOf(false, true)) {
                val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "r3r-identity-$unnamed-$clientFirst-")
                val home = directory.resolve("private-home")
                prepare(directory, home, "identity")
                Files.copy(
                    Path.of("integration-test/mixin-compilation/identities.gradle"),
                    directory.resolve("build.gradle"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
                println("R3R_IDENTITY unnamed=$unnamed clientFirst=$clientFirst $directory")
                val runner =
                    GradleRunner
                        .create()
                        .withProjectDir(directory.toFile())
                        .withTestKitDir(home.toFile())
                        .withArguments(
                            listOf(
                                "verifyIdentities",
                                "--console=plain",
                                "--stacktrace",
                                "--max-workers=2",
                                "--no-configuration-cache",
                                "-g",
                                home.toString(),
                                "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            ) +
                                (if (unnamed) listOf("-PidentityUnnamed=true") else emptyList()) +
                                (if (clientFirst) listOf("-PidentityClientFirst=true") else emptyList()),
                        )
                try {
                    val result = runner.build()
                    Files.writeString(directory.resolve("gradle.log"), result.output)
                    assertTrue(result.output.contains("VERIFIED_IDENTITIES"), result.output)
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun nativeProjectConsumersRetainCompilationRuntimeAndSources() {
        val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "r3r-project-consumers-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "stub-on")
        Files.copy(
            Path.of("integration-test/mixin-compilation/stubs.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(Path.of("integration-test/mixin-compilation/project-consumers.gradle"), directory.resolve("project-consumers.gradle"))
        Files.writeString(
            directory.resolve("build.gradle"),
            "\napply from: 'project-consumers.gradle'\n",
            java.nio.file.StandardOpenOption.APPEND,
        )
        println("R3R_PROJECT_CONSUMERS $directory")
        val runner =
            GradleRunner
                .create()
                .withProjectDir(directory.toFile())
                .withTestKitDir(home.toFile())
                .withArguments(
                    "verifyProjectConsumers",
                    "--info",
                    "--console=plain",
                    "--stacktrace",
                    "--max-workers=2",
                    "--no-configuration-cache",
                    "-g",
                    home.toString(),
                    "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    "-Pscenario=stub-on",
                )
        try {
            val result = runner.build()
            Files.writeString(directory.resolve("gradle.log"), result.output)
            assertTrue(result.output.contains("VERIFIED_PROJECT_CONSUMERS 5 2"), result.output)
            assertTrue(result.output.contains("LOCAL_SOURCE_MATCH true"))
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun commonStubsIntersectEffectiveParticipantsAndFallbacks() = verifyCommonStubs(false)

    @Test
    fun explicitStubParticipantsDoNotShareSourcesOrRuntime() {
        var baseline: String? = null
        for (mode in listOf("baseline", "explicit", "duplicates")) {
            val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "stub-from-$mode-")
            val home = directory.resolve("private-home")
            prepare(directory, home, "stub-from")
            Files.delete(directory.resolve("build.gradle"))
            Files.delete(directory.resolve("src/main/java/Consume.java"))
            val fixtures = Path.of("integration-test/mixin-compilation")
            Files.copy(fixtures.resolve("stub-from.gradle.kts"), directory.resolve("build.gradle.kts"))
            Files.copy(fixtures.resolve("stub-from-observe.gradle"), directory.resolve("observe.gradle"))
            val runner = GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile())
            fun run(label: String, vararg tasks: String) = try {
                runner.withArguments(listOf("--offline", "--no-build-cache", "--no-configuration-cache", "--stacktrace", "--max-workers=2",
                    "-g", home.toString(), "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}", "-Pmode=$mode") + tasks)
                    .build().also { Files.writeString(directory.resolve("$label.log"), it.output) }
            } catch (e: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("$label.log"), e.buildResult.output)
                throw e
            }
            val cold = run("cold", "createSharedApiStub")
            assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, cold.task(":createSharedApiStub")?.outcome)
            assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, cold.task(":accessWidenLeftMinecraft")?.outcome)
            for (producer in listOf(":resolveRightPatchedMinecraft", ":accessWidenRightMinecraft")) {
                if (mode == "baseline") assertNull(cold.task(producer))
                else assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, cold.task(producer)?.outcome)
            }
            assertFalse(cold.tasks.any { it.path.startsWith(":accessWidenLeftClient") || it.path.startsWith(":accessWidenRightData") })
            assertTrue(cold.tasks.none { it.path.startsWith(":preapply") })
            val remaining = listOf("SharedData", "SharedTest", "SharedClient", "SharedClientData", "SharedClientTest", "Control")
            run("platforms", *remaining.map { "create${it}ApiStub" }.toTypedArray(), "observePlatforms")
            val platforms = Files.readString(directory.resolve("platforms.json"))
            if (baseline == null) baseline = platforms else assertEquals(baseline, platforms)
        }
    }

    @Test
    fun commonMainStubsIntersectEffectiveParticipants() = verifyCommonStubs(true)

    private fun verifyCommonStubs(mainOnly: Boolean) {
        for (scenario in if (mainOnly) listOf("stub-main-on", "stub-main-off") else listOf("stub-on", "stub-off", "stub-file")) {
            val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "r3-$scenario-")
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            Files.copy(
                Path.of("integration-test/mixin-compilation/stubs.gradle"),
                directory.resolve("build.gradle"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            val originals =
                Files.walk(directory).use { files ->
                    files.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
                }
            println("R3_STUBS $scenario $directory")
            val runner =
                GradleRunner
                    .create()
                    .withProjectDir(directory.toFile())
                    .withTestKitDir(home.toFile())
                    .withArguments(
                        "verifyStubs",
                        "--info",
                        "--offline",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        "-Pscenario=$scenario",
                    )
            try {
                val result = runner.build()
                Files.writeString(directory.resolve("gradle.log"), result.output)
                val count = if (mainOnly) 1 else 6
                val off = scenario.endsWith("off")
                assertTrue(result.output.contains("VERIFIED_STUBS $scenario $count"), result.output)
                assertEquals(count, result.tasks.count { it.path.startsWith(":createCommon") && it.path.endsWith("ApiStub") })
                assertTrue(result.output.contains("LOCAL_SOURCE_MATCH true"))
                assertEquals(
                    if (mainOnly) {
                        if (off) 2 else 4
                    } else {
                        if (off) 7 else 11
                    },
                    result.tasks.count { it.path.startsWith(":preapply") },
                )
                assertFalse(result.tasks.any { it.path.startsWith(":preapplyCommon") })
                if (off) assertFalse(result.tasks.any { it.path.startsWith(":preapplyRight") || it.path.startsWith(":joinRight") })
                assertNull(result.task(":compileLeftJava"))
                assertNull(result.task(":compileRightJava"))
                originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun commonStubConsumesFullPreparationAndBlocksFailures() {
        for (scenario in listOf("stub-on", "stub-off", "stub-all-off")) {
            for (fault in if (scenario == "stub-on") listOf("none", "native", "mixin", "record") else listOf("none")) {
                val directory =
                    Files.createTempDirectory(
                        Path.of(System.getProperty("fixture.metadata")).parent.parent,
                        "i-common-$scenario-$fault-",
                    )
                val home = directory.resolve("private-home")
                prepare(directory, home, scenario, classifier = true)
                Files.copy(
                    Path.of("integration-test/mixin-compilation/stubs.gradle"),
                    directory.resolve("build.gradle"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
                Files.copy(
                    Path.of("integration-test/mixin-compilation/common-preparation.gradle"),
                    directory.resolve("common-preparation.gradle"),
                )
                Files.writeString(
                    directory.resolve("build.gradle"),
                    "\napply from: 'common-preparation.gradle'\n",
                    java.nio.file.StandardOpenOption.APPEND,
                )
                println("I_COMMON_STUB $scenario $fault $directory")
                val args =
                    listOf(
                        "verifyStubs",
                        "--offline",
                        "--info",
                        "--console=plain",
                        "--stacktrace",
                        "--max-workers=2",
                        "--no-configuration-cache",
                        "-g",
                        home.toString(),
                        "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                        "-Pscenario=$scenario",
                    )

                fun runner(extra: List<String>) =
                    GradleRunner
                        .create()
                        .withProjectDir(directory.toFile())
                        .withTestKitDir(home.toFile())
                        .withArguments(args + extra)
                try {
                    if (fault != "none") {
                        val failed = runner(listOf("-PcommonFault=$fault")).buildAndFail()
                        Files.writeString(directory.resolve("expected-$fault.log"), failed.output)
                        assertFalse(failed.output.contains("COMMON_STUB_PREPARATION_READY :createCommonApiStub"), failed.output)
                        assertFalse(failed.output.contains("R3_STUB_INPUT_READY :createCommonApiStub"), failed.output)
                        assertFalse(failed.output.contains("VERIFIED_STUBS"), failed.output)
                        assertTrue(
                            failed.output.contains(
                                when (fault) {
                                    "native" -> "common_native_failure"
                                    "mixin" -> "common_mixin_failure"
                                    else -> "applied-mixins"
                                },
                            ),
                            failed.output,
                        )
                    }
                    val result = runner(if (fault == "none") emptyList() else listOf("--rerun-tasks")).build()
                    Files.writeString(directory.resolve("gradle.log"), result.output)
                    assertTrue(result.output.contains("VERIFIED_COMMON_PREPARATION"), result.output)
                    assertTrue(result.output.contains("VERIFIED_STUBS $scenario 6"), result.output)
                    if (scenario == "stub-all-off") assertFalse(result.output.contains("R3_OPERATION"), result.output)
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun commonRelationsKeepGroupedLinksAndLazyRunFallbacks() {
        for (mode in listOf("existing", "late", "missing-data", "missing-test")) {
            val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "i-common-g5-$mode-")
            val home = directory.resolve("private-home")
            prepare(directory, home, "stub-on")
            Files.copy(
                Path.of("integration-test/mixin-compilation/common-relations.gradle"),
                directory.resolve("build.gradle"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            println("I_COMMON_G5 $mode $directory")
            val runner =
                GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile()).withArguments(
                    if (mode.startsWith("missing")) "help" else "verifyCommonRelations",
                    "--offline",
                    "--info",
                    "--console=plain",
                    "--stacktrace",
                    "--max-workers=2",
                    "--no-configuration-cache",
                    "-g",
                    home.toString(),
                    "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    "-PcommonRelations=$mode",
                )
            try {
                val result = if (mode.startsWith("missing")) runner.buildAndFail() else runner.build()
                Files.writeString(directory.resolve("gradle.log"), result.output)
                if (mode.startsWith("missing")) {
                    // The original afterEvaluate diagnostic uses the root project's name.
                    assertTrue(
                        result.output.contains("Run configuration 'mixin-compilation' did not have pre-requisite source sets configured."),
                        result.output,
                    )
                    assertTrue(
                        result.output.contains("Please add '${if (mode == "missing-data") "data" else "test"}()' to the target 'left'."),
                        result.output,
                    )
                    assertTrue(
                        result.output.contains("RunConfigurationsKt\$withCompilation\$1.execute(RunConfigurations.kt:43)"),
                        result.output,
                    )
                } else {
                    assertTrue(result.output.contains("VERIFIED_COMMON_RELATIONS $mode"), result.output)
                }
            } catch (failure: UnexpectedBuildFailure) {
                Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                throw failure
            }
        }
    }

    @Test
    fun standaloneSyncUsesCompletePreparationAndIndependentRuntime() {
        for (scenario in listOf("on", "off")) {
            for (sources in listOf(false, true)) {
                val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "i-sync-standalone-$scenario-$sources-")
                val home = directory.resolve("private-home")
                prepare(directory, home, scenario)
                if (sources) seedCompilerDependencies(home)
                if (sources && scenario == "on") {
                    Files.writeString(directory.resolve("src/main/java/Consume.java"), "public class Consume { Populated p; }")
                    Files.writeString(directory.resolve("src/main/kotlin/ConsumeKotlin.kt"), "class ConsumeKotlin(val p: Populated)\n")
                }
                Files.copy(Path.of("integration-test/mixin-compilation/sync.gradle"), directory.resolve("sync.gradle"))
                Files.copy(directory.resolve("repository/ordinary/ref/1/ref-1.jar"), directory.resolve("repository/ordinary/ref/1/ref-1-extra.jar"))
                Files.writeString(directory.resolve("build.gradle"), "\napply from: 'sync.gradle'\n", java.nio.file.StandardOpenOption.APPEND)
                assertFalse(Files.exists(directory.resolve("build")))
                assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
                println("I_SYNC_STANDALONE $scenario sources=$sources $directory")
                val args = mutableListOf("clocheIdeSync", "--offline", "--info", "--console=plain", "--stacktrace", "--max-workers=2", "--no-configuration-cache", "-g", home.toString(), "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}", "-Pscenario=$scenario")
                if (sources) args.add("-PsyncSources=true")
                val runner = GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile())
                try {
                    if (scenario == "off" && !sources) {
                        val failed = runner.withArguments(args + "-PsyncNativeFault=true").buildAndFail()
                        Files.writeString(directory.resolve("native-failure.log"), failed.output)
                        assertTrue(failed.output.contains("sync_native_failure"), failed.output)
                        assertFalse(failed.output.contains("VERIFIED_SYNC"), failed.output)
                        assertFalse(Files.exists(directory.resolve("sync-observation.json")))
                    }
                    val result = runner.withArguments(args).build()
                    Files.writeString(directory.resolve("sync.log"), result.output)
                    assertNotNull(result.task(":clocheIdeSync"))
                    assertTrue(result.output.contains("VERIFIED_SYNC $scenario"), result.output)
                    if (!sources) assertFalse(result.tasks.any { it.path in listOf(":compileJava", ":compileKotlin") })
                    assertTrue(Files.isRegularFile(directory.resolve("sync-observation.json")))
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("unexpected-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun syncToolingBaseline() {
        runSyncTooling("baseline")
    }

    @Test
    fun syncToolingOff() {
        runSyncTooling("off")
    }

    @Test
    fun syncToolingOn() {
        runSyncTooling("on")
    }

    @Test
    fun syncToolingFile() {
        runSyncTooling("file")
    }

    @Test
    fun syncToolingSources() {
        runSyncTooling("on", sources = true)
        runSyncTooling("off", sources = true)
    }

    @Test
    fun syncToolingStopsAfterFailedPreparation() {
        runSyncTooling("on", fault = true)
    }

    @Test
    fun syncToolingFullCommonModels() {
        runSyncTooling("stub-on", fullCommon = true)
        runSyncTooling("stub-off", fullCommon = true)
    }

    private fun runSyncTooling(
        scenario: String,
        sources: Boolean = false,
        fault: Boolean = false,
        fullCommon: Boolean = false,
    ) {
        val label =
            if (fault) {
                "fault"
            } else if (sources) {
                "sources"
            } else {
                scenario
            }
        val directory = Files.createTempDirectory(Path.of(System.getProperty("fixture.metadata")).parent.parent, "r4m-tooling-$label-")
        val home = directory.resolve("private-home")
        prepare(directory, home, scenario)
        if (sources) seedCompilerDependencies(home)
        Files.writeString(directory.resolve("src/main/java/Consume.java"), "public class Consume { Populated p; }")
        Files.writeString(directory.resolve("src/main/kotlin/ConsumeKotlin.kt"), "class ConsumeKotlin(val p: Populated)\n")
        if (fullCommon) {
            Files.copy(
                Path.of("integration-test/mixin-compilation/stubs.gradle"),
                directory.resolve("build.gradle"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
        }
        val observer = if (fullCommon) "full-model.gradle" else "sync.gradle"
        for (name in listOf(observer, "observe.init.gradle", "R4Tooling.java")) {
            Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
        }
        Files.copy(directory.resolve("repository/ordinary/ref/1/ref-1.jar"), directory.resolve("repository/ordinary/ref/1/ref-1-extra.jar"))
        Files.writeString(directory.resolve("build.gradle"), "\napply from: '$observer'\n", java.nio.file.StandardOpenOption.APPEND)
        assertFalse(Files.exists(directory.resolve("build")))
        assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
        val originals =
            Files.walk(directory).use { files ->
                files.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.toList().associateWith(Files::readAllBytes)
            }
        println("R4M_TOOLING $label $directory")
        val testKit =
            Path
                .of(
                    GradleRunner::class.java.protectionDomain.codeSource.location
                        .toURI(),
                ).toRealPath()
        // DefaultGradleRunner uses this internal locator; generated TestKit JARs are not installation roots.
        val runnerLoader = GradleRunner::class.java.classLoader
        val current = runnerLoader.loadClass("org.gradle.internal.installation.CurrentGradleInstallation")
        val locator = runnerLoader.loadClass("org.gradle.internal.installation.CurrentGradleInstallationLocator")
        val beacon = locator.classLoader.loadClass("org.gradle.internal.installation.beacon.InstallationBeacon")
        check(
            GradleRunner
                .create()
                .javaClass.classLoader
                .loadClass(current.name) === current &&
                current.classLoader.loadClass(locator.name) === locator && runnerLoader.loadClass(beacon.name) === beacon,
        ) {
            "Default Runner installation class space differs: TestKit=$testKit Current=$current Locator=$locator Beacon=$beacon"
        }
        val installationSources =
            listOf(current, locator, beacon).associate { type ->
                val source =
                    checkNotNull(type.protectionDomain.codeSource) { "Missing installation source: ${type.name}, TestKit=$testKit" }
                type.name to
                    Path.of(source.location.toURI()).toRealPath().also {
                        check(Files.isRegularFile(it)) { "Missing installation source JAR: ${type.name} $it, TestKit=$testKit" }
                    }
            }
        val installation =
            checkNotNull(current.getMethod("get").invoke(null)) {
                "Default Runner installation is null: TestKit=$testKit Sources=$installationSources"
            }
        check(installation.javaClass === runnerLoader.loadClass("org.gradle.internal.installation.GradleInstallation")) {
            "Unexpected installation type: ${installation.javaClass}, TestKit=$testKit Sources=$installationSources"
        }
        val gradleHome = (installation.javaClass.getMethod("getGradleHome").invoke(installation) as File).toPath().toRealPath()
        val lib = gradleHome.resolve("lib").toRealPath()
        check(
            Files.isDirectory(lib.resolve("plugins")) && installationSources.getValue(beacon.name).parent == lib &&
                Files.list(lib).use { files ->
                    files.anyMatch {
                        Files.isRegularFile(it) &&
                            it.fileName.toString().startsWith("gradle-tooling-api-") &&
                            it.toString().endsWith(".jar")
                    }
                },
        ) {
            "Default Runner installation/tool libraries differ: Home=$gradleHome TestKit=$testKit Sources=$installationSources"
        }
        println(
            "WORKER_INSTALLATION TestKit=$testKit RunnerLoader=$runnerLoader LocatorLoader=${locator.classLoader} Sources=$installationSources Home=$gradleHome",
        )
        val javaHome = Path.of(System.getProperty("java.home")).toRealPath()
        val environmentFile = directory.resolve("environment.properties")
        val environment = Properties().apply { Files.newInputStream(environmentFile).use(::load) }
        environment.setProperty("gradleHome", gradleHome.toString())
        environment.setProperty("workerJavaHome", javaHome.toString())
        installationSources.forEach { (type, source) -> environment.setProperty(type, source.toString()) }
        Files.newOutputStream(environmentFile).use { environment.store(it, "Actual test worker sources") }
        for (tool in listOf("java", "javac")) {
            check(Files.isRegularFile(javaHome.resolve("bin/$tool.exe"))) { "Missing worker JDK tool: $javaHome/bin/$tool.exe" }
        }

        fun runTool(
            executable: String,
            vararg args: String,
        ): Int {
            val log = directory.resolve("$executable.log").toFile()
            val process =
                ProcessBuilder(listOf("$javaHome/bin/$executable.exe") + args)
                    .directory(directory.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(log)
                    .start()
            return process.waitFor()
        }
        assertEquals(
            0,
            runTool("javac", "-cp", "$lib/*", "-d", directory.toString(), directory.resolve("R4Tooling.java").toString()),
            Files.readString(directory.resolve("javac.log")),
        )
        val options =
            buildList {
                if (sources) add("-PsyncSources=true")
                if (fault) add("-Pr4Fault=true")
            }
        var result =
            runTool(
                "java",
                "-cp",
                "$directory${File.pathSeparator}$lib/*",
                "R4Tooling",
                directory.toString(),
                scenario,
                *options.toTypedArray(),
            )
        if (fault) {
            assertNotEquals(0, result)
            val log = Files.readString(directory.resolve("model.log"))
            assertTrue(log.contains("r4_missing_namespace"), log)
            assertTrue(log.contains("preapply"), log)
            assertFalse(log.contains("R4M_BUILDER"), log)
            assertFalse(Files.exists(directory.resolve("model.json")))
            val partialOutputs =
                Files.walk(directory.resolve("build")).use { files ->
                    files.filter { Files.isRegularFile(it) }.toList().joinToString("\n") {
                        "${directory.relativize(it)} ${Files.size(it)} bytes"
                    }
                }
            Files.writeString(directory.resolve("failure-files.txt"), partialOutputs)
            for (name in listOf("java", "model")) Files.move(directory.resolve("$name.log"), directory.resolve("$name-failure.log"))
            result =
                runTool(
                    "java",
                    "-cp",
                    "$directory${File.pathSeparator}$lib/*",
                    "R4Tooling",
                    directory.toString(),
                    scenario,
                )
        }
        assertEquals(0, result, Files.readString(directory.resolve("java.log")))
        assertTrue(Files.isRegularFile(directory.resolve("model.json")))
        val log = Files.readString(directory.resolve("model.log"))
        val marker = if (fullCommon) "VERIFIED_FULL_MODEL_INPUTS" else "VERIFIED_SYNC $scenario"
        assertTrue(log.contains(marker), log)
        assertTrue(log.indexOf(marker) < log.indexOf("R4M_BUILDER"), log)
        assertTrue(log.contains("R4M_STRICT_READ_COMPLETE"), log)
        val model = Files.readString(directory.resolve("model.json"))
        assertTrue(model.contains("GradleSourceSetDependencyModel"))
        val observation = Files.readString(directory.resolve(if (fullCommon) "full-model-observation.json" else "sync-observation.json"))
        Regex("\"clean\": \"([^\"]+)\"").findAll(observation).forEach {
            assertTrue(model.contains(it.groupValues[1]), "Clean sync artifact missing from complete native model: ${it.groupValues[1]}")
        }
        originals.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
    }

    @Test
    fun nestedAwAndSourcesUseUniqueDeclaredPaths() {
        for (unnamed in listOf(false, true)) {
            for (off in listOf(false, true)) {
                val scenario = (if (unnamed) "" else "stub-") + (if (off) "off" else "on")
                val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "r4p-paths-$unnamed-$off-")
                val home = directory.resolve("private-home")
                prepare(directory, home, scenario)
                if (!unnamed) {
                    Files.copy(
                        Path.of("integration-test/mixin-compilation/stubs.gradle"),
                        directory.resolve("build.gradle"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    )
                }
                Files.copy(Path.of("integration-test/mixin-compilation/paths.gradle"), directory.resolve("paths.gradle"))
                val captureTarget = if (unnamed) "\next.pathTarget = target\n" else "\n"
                Files.writeString(
                    directory.resolve("build.gradle"),
                    captureTarget + "apply from: 'paths.gradle'\n",
                    java.nio.file.StandardOpenOption.APPEND,
                )
                assertFalse(Files.exists(directory.resolve("build")))
                assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
                println("R4P_PATHS unnamed=$unnamed off=$off $directory")
                val runner =
                    GradleRunner
                        .create()
                        .withProjectDir(directory.toFile())
                        .withTestKitDir(home.toFile())
                        .withArguments(
                            "verifyPaths",
                            "--info",
                            "--console=plain",
                            "--stacktrace",
                            "--max-workers=2",
                            "--no-configuration-cache",
                            "-g",
                            home.toString(),
                            "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                            "-Pscenario=$scenario",
                        )
                try {
                    val first = runner.build()
                    Files.writeString(directory.resolve("paths-first.log"), first.output)
                    assertTrue(first.output.contains("VERIFIED_PATHS"), first.output)
                    assertEquals(6, first.tasks.count { it.path.startsWith(":decompile") })
                    assertTrue(first.output.contains("LOCAL_SOURCE_MATCH true") == !off)
                    if (off) {
                        assertFalse(first.tasks.any { it.path.startsWith(":preapply") || it.path.startsWith(":join") })
                        assertFalse(first.output.contains("Caching disabled for SelectMixinSources:"))
                        assertFalse(first.output.contains("Caching disabled for StripMixins:"))
                    }
                    val repeat = runner.build()
                    Files.writeString(directory.resolve("paths-repeat.log"), repeat.output)
                    assertTrue(repeat.output.contains("VERIFIED_PATHS"), repeat.output)
                    assertTrue(
                        repeat.tasks.filter { it.path.startsWith(":decompile") }.all {
                            it.outcome ==
                                org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
                        },
                    )
                } catch (failure: UnexpectedBuildFailure) {
                    Files.writeString(directory.resolve("paths-failure.log"), failure.buildResult.output)
                    throw failure
                }
            }
        }
    }

    @Test
    fun syncFullCommonFallbacksAndRuntimePrepareBeforeModel() {
        val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "r4-full-sync-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "stub-on")
        Files.copy(
            Path.of("integration-test/mixin-compilation/stubs.gradle"),
            directory.resolve("build.gradle"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(Path.of("integration-test/mixin-compilation/full-sync-paths.gradle"), directory.resolve("full-sync-paths.gradle"))
        Files.writeString(
            directory.resolve("build.gradle"),
            "\nidea.module.downloadSources = false\napply from: 'full-sync-paths.gradle'\n",
            java.nio.file.StandardOpenOption.APPEND,
        )
        println("R4_FULL_SYNC $directory")
        val runner =
            GradleRunner
                .create()
                .withProjectDir(directory.toFile())
                .withTestKitDir(home.toFile())
                .withArguments(
                    "clocheIdeSync",
                    "--info",
                    "--console=plain",
                    "--stacktrace",
                    "--max-workers=2",
                    "--no-configuration-cache",
                    "-g",
                    home.toString(),
                    "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
                    "-Pscenario=stub-on",
                )
        try {
            val result = runner.build()
            Files.writeString(directory.resolve("prepare.log"), result.output)
            assertNotNull(result.task(":clocheIdeSync"))
            assertTrue(result.output.contains("VERIFIED_FULL_SYNC"), result.output)
            assertTrue(Files.isRegularFile(directory.resolve("full-sync-observation.json")))
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("prepare.log"), failure.buildResult.output)
            throw failure
        }
    }

    @Test
    fun recoveryV67TracksContextAndContinuousInputs() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        val directory = Files.createTempDirectory(invocation, "v67-context-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "file")
        seedCompilerDependencies(home)
        val build = directory.resolve("build.gradle")
        val originalBuild = Files.readString(build)
        // This separate refmap-only input can legally select an explicit context.
        val recoveryBuild = originalBuild.replace(
            "d.modImplementation.add(project.files('repository/probe/plain/1/plain-1.jar', 'repository/probe/transitive/1/transitive-1.jar'))",
            "if (providers.gradleProperty('v67Member').isPresent()) d.modImplementation.add(project.files('repository/probe/transitive/1/transitive-1.jar'))",
        )
        check(recoveryBuild != originalBuild)
        Files.writeString(build, recoveryBuild + "\napply from: 'recovery-v67.gradle'\n")
        Files.copy(Path.of("integration-test/mixin-compilation/recovery-v67.gradle"), directory.resolve("recovery-v67.gradle"))
        Files.writeString(directory.resolve("src/main/java/Consume.java"), "public class Consume { int use(named.Game g) { probe.Ref r = g; return r.callRef(); } }\n")
        Files.writeString(directory.resolve("src/main/kotlin/ConsumeKotlin.kt"), "fun use(g: named.Game): Int { val r: probe.Ref = g; return r.callRef() }\n")
        val source = directory.resolve("repository/probe/ref/1/ref-1.jar")
        fun rewriteJar(path: Path, change: (MutableMap<String, ByteArray>) -> Unit) {
            val entries = java.util.zip.ZipFile(path.toFile()).use { zip ->
                zip.entries().asSequence().filterNot { it.isDirectory }.associateTo(linkedMapOf()) { it.name to zip.getInputStream(it).readBytes() }
            }
            change(entries)
            JarOutputStream(Files.newOutputStream(path)).use { jar ->
                entries.forEach { (name, bytes) ->
                    jar.putNextEntry(JarEntry(name).apply { time = 0 })
                    jar.write(bytes)
                    jar.closeEntry()
                }
            }
        }
        rewriteJar(source) { entries ->
            entries["root-refmap.json"] = """{"mappings":{"probe/Ref":{"alias":"Lobf/Game;a()I"}},"data":{"chosen":{"probe/Ref":{"alias":"Lobf/Game;a()I"}},"other":{"probe/Ref":{"alias":"Lobf/Game;b()I"}}}}""".toByteArray()
        }
        val baselineSource = Files.readAllBytes(source)
        rewriteJar(directory.resolve("repository/probe/transitive/1/transitive-1.jar")) { entries ->
            entries["transitive.json"] = """{"required":true,"package":"transitive","mixins":[],"refmap":"root-refmap.json"}""".toByteArray()
            entries["root-refmap.json"] = """{"mappings":{},"data":{"chosen":{}}}""".toByteArray()
        }
        val protected = listOf(directory.resolve("input/common.jar"), directory.resolve("input/client.jar"), source,
            directory.resolve("input/mappings.tiny"), directory.resolve("input/game.accesswidener"))
        val runner = GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile())
        fun run(label: String, expectedFailure: String? = null, vararg options: String) {
            val before = protected.associateWith(Files::readAllBytes)
            val args = listOf("verifyV67", "--offline", "--info", "--stacktrace", "--console=plain", "--max-workers=2", "--no-configuration-cache", "-g", home.toString(),
                "-Pscenario=file", "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}", "-Pv67Label=$label") + options
            val result = if (expectedFailure == null) runner.withArguments(args).build() else runner.withArguments(args).buildAndFail()
            Files.writeString(directory.resolve("$label.log"), result.output)
            Files.writeString(directory.resolve("$label-tasks.txt"), result.tasks.joinToString("\n") { "${it.path} ${it.outcome}" })
            before.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(path), path.toString()) }
            if (expectedFailure == null) {
                assertTrue(result.output.contains("VERIFIED_V67 $label"), result.output)
            } else {
                assertTrue(result.output.contains(expectedFailure), result.output)
                assertFalse(result.output.contains("VERIFIED_V67"), result.output)
                assertNull(result.task(":compileJava"))
                assertNull(result.task(":compileKotlin"))
            }
        }
        println("V67_WORKSPACE $directory")
        assertFalse(Files.exists(directory.resolve("build")))
        assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
        run("baseline")
        run("repeat")
        run("missing-context", "Unknown refmap context", "-Pv67Context=missing")
        run("context-recovery")
        run("other-context", null, "-Pv67Context=other", "-Pv67Expected=22")
        run("other-recovery")
        run("missing-namespace", "v67_missing_namespace", "-Pv67Namespace=v67_missing_namespace")
        run("namespace-recovery")
        rewriteJar(source) { it["keep.txt"] = "changed source resource".toByteArray() }
        run("source-content")
        Files.write(source, baselineSource)
        run("content-recovery")
        rewriteJar(source) { it["ref.json"] = String(it.getValue("ref.json")).replace("\"required\":true", "\"required\":false").toByteArray() }
        run("source-config")
        Files.write(source, baselineSource)
        run("config-recovery")
        rewriteJar(source) { it["fabric.mod.json"] = String(it.getValue("fabric.mod.json")).replace("ref.json", "missing-v67.json").toByteArray() }
        run("missing-config", "missing-v67.json")
        Files.write(source, baselineSource)
        run("missing-config-recovery")
        rewriteJar(source) { it["ref.json"] = "{".toByteArray() }
        run("invalid-config", "Expected end of the object '}', but had 'EOF'")
        Files.write(source, baselineSource)
        run("invalid-config-recovery")
        run("source-member", null, "-Pv67Member=true")
        run("member-recovery")
        val relocated = directory.resolve("relocated/ref-1.jar")
        Files.createDirectories(relocated.parent)
        Files.write(relocated, baselineSource)
        Files.writeString(build, Files.readString(build).replace("repository/probe/ref/1/ref-1.jar", "relocated/ref-1.jar"))
        run("source-position")
        Files.writeString(build, recoveryBuild + "\napply from: 'recovery-v67.gradle'\n")
        run("position-recovery")
        val aw = directory.resolve("input/game.accesswidener")
        val originalAw = Files.readString(aw)
        Files.writeString(aw, originalAw + "accessible method named/Game namedA ()I\n")
        run("aw-content", null, "-Pv67PublicMethod=namedA")
        Files.writeString(aw, originalAw)
        run("aw-recovery")
        run("side", null, "-Pv67Side=SERVER")
        run("side-recovery")
        val mapping = directory.resolve("input/mappings.tiny")
        val originalMapping = Files.readString(mapping)
        Files.writeString(mapping, originalMapping.replace("\tnamedA", "\trenamedA"))
        run("mapping-content", null, "-Pv67MappedMethod=renamedA")
        Files.writeString(mapping, originalMapping)
        run("mapping-recovery")
        run("new-process", null, "-Dorg.gradle.jvmargs=-Xmx768m")
        val pid = Regex("\\\"pid\\\"\\s*:\\s*(\\d+)")
        assertNotEquals(
            pid.find(Files.readString(directory.resolve("mapping-recovery.json")))!!.groupValues[1],
            pid.find(Files.readString(directory.resolve("new-process.json")))!!.groupValues[1],
        )
    }

    @Test
    fun recoveryV67TracksExactDirectSourceLocation() {
        val invocation = Path.of(System.getProperty("fixture.metadata")).parent.parent
        val directory = Files.createTempDirectory(invocation, "v67-location-")
        val home = directory.resolve("private-home")
        prepare(directory, home, "direct")
        seedCompilerDependencies(home)
        val build = directory.resolve("build.gradle")
        val initial = Files.readString(build).replace("d.modCompileOnly.add('probe:ref:1')", "d.modCompileOnly.add(project.files('repository/probe/ref/1/ref-1.jar'))")
        val original = directory.resolve("repository/probe/ref/1/ref-1.jar")
        val relocated = directory.resolve("relocated/ref-1.jar")
        Files.createDirectories(relocated.parent)
        Files.copy(original, relocated)
        for (label in listOf("baseline", "relocated", "recovery")) {
            Files.writeString(build, if (label == "relocated") initial.replace("repository/probe/ref/1/ref-1.jar", "relocated/ref-1.jar") else initial)
            val result = GradleRunner.create().withProjectDir(directory.toFile()).withTestKitDir(home.toFile()).withArguments(
                "verifyCompile", "--offline", "--info", "--stacktrace", "--console=plain", "--max-workers=2", "--no-configuration-cache", "-g", home.toString(),
                "-Pscenario=direct", "-PclocheClasspathMetadata=${directory.resolve("classpath.properties")}",
            ).build()
            Files.writeString(directory.resolve("$label.log"), result.output)
            Files.copy(directory.resolve("observation.json"), directory.resolve("$label.json"))
            assertTrue(result.output.contains("VERIFIED_COMPILE direct"), result.output)
            assertArrayEquals(Files.readAllBytes(original), Files.readAllBytes(relocated))
        }
        println("V67_LOCATION_WORKSPACE $directory")
    }

    @Test
    fun prepareFinalAcceptanceInputs() {
        val fixtures = linkedMapOf<String, String>()
        for ((label, scenario) in linkedMapOf(
            "main" to "file",
            "context" to "on",
            "empty" to "direct",
            "stub" to "stub-on",
            "file-C" to "file",
            "file-View" to "file",
            "file-SourceSet" to "file",
        )) {
            val directory = Files.createTempDirectory(Path.of("build/tmp").toAbsolutePath(), "r5-$label-")
            val home = directory.resolve("private-home")
            prepare(directory, home, scenario)
            assertFalse(Files.exists(directory.resolve("build")))
            assertFalse(Files.exists(home.resolve("caches/9.5.1/transforms")))
            fixtures[label] = directory.toString().replace('\\', '/')
            println("R5_PREPARED $label $scenario $directory")
        }
        Files.writeString(
            Path.of("build/reports/mixin-r5/fixtures.properties"),
            fixtures.entries.joinToString("\n") { "${it.key}=${it.value}" },
        )
    }

    private fun seedCompilerDependencies(home: Path) {
        val modules =
            listOf(
                "org.jetbrains:annotations:13.0",
                "org.jetbrains.kotlin:kotlin-reflect:1.6.10",
                "org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.0",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0",
            ) +
                listOf(
                    "build-tools-api",
                    "build-tools-compat",
                    "build-tools-cri-impl",
                    "build-tools-impl",
                    "compiler-embeddable",
                    "compiler-runner",
                    "daemon-client",
                    "daemon-embeddable",
                    "script-runtime",
                    "scripting-common",
                    "scripting-compiler-embeddable",
                    "scripting-compiler-impl-embeddable",
                    "scripting-jvm",
                    "stdlib",
                    "tooling-core",
                ).map { "org.jetbrains.kotlin:kotlin-$it:2.3.20" }
        seedDependencies(home, modules)
    }

    private fun seedDependencies(
        home: Path,
        coordinates: List<String>,
    ) {
        // Copy immutable dependency inputs only; transforms and task outputs must be produced in this fixture.
        val dependencyCache = Path.of(System.getProperty("fixture.gradleUserHome"), "caches/modules-2")
        val cacheInputs =
            coordinates.flatMap { coordinate ->
                val path = coordinate.replace(':', '/')
                listOf(dependencyCache.resolve("metadata-2.107/descriptors/$path")) +
                    (
                        if (coordinate.endsWith(
                                ":kotlinx-coroutines-bom:1.8.0",
                            )
                        ) {
                            emptyList()
                        } else {
                            listOf(dependencyCache.resolve("files-2.1/$path"))
                        }
                    )
            } + Files.list(dependencyCache.resolve("metadata-2.107")).use { paths -> paths.filter(Files::isRegularFile).toList() }
        cacheInputs.forEach { input ->
            assertTrue(Files.exists(input), input.toString())
            Files.walk(input).use { paths ->
                paths.filter(Files::isRegularFile).forEach { source ->
                    val destination = home.resolve("caches/modules-2").resolve(dependencyCache.relativize(source))
                    Files.createDirectories(destination.parent)
                    val bytes = Files.readAllBytes(source)
                    Files.write(destination, bytes)
                    assertArrayEquals(bytes, Files.readAllBytes(source), "Dependency changed while copying: $source")
                }
            }
        }
    }

    private fun prepare(
        directory: Path,
        home: Path,
        scenario: String,
        classifier: Boolean = false,
    ) {
        val prepareStarted = System.nanoTime()
        Files.createDirectories(home.resolve("init.d"))
        Files.copy(Path.of("integration-test/mixin-compilation/invocation.gradle"), home.resolve("init.d/fixture-invocation.gradle"))
        val testKit =
            Path
                .of(
                    GradleRunner::class.java.protectionDomain.codeSource.location
                        .toURI(),
                ).toRealPath()
        val environment =
            Properties().apply {
                setProperty("workerJavaHome", File(System.getProperty("java.home")).canonicalPath)
                setProperty("testKit", testKit.toString())
                setProperty(
                    "gradleVersion",
                    org.gradle.util.GradleVersion
                        .current()
                        .version,
                )
                setProperty("privateHome", home.toFile().canonicalPath)
            }
        assertEquals(17, Runtime.version().feature())
        Files.newOutputStream(directory.resolve("environment.properties")).use { environment.store(it, "Actual test worker sources") }
        println("WORKER_ENV $environment")
        if (scenario.startsWith("stub-")) {
            seedDependencies(
                home,
                listOf("net.msrandom:java-expect-actual-annotations:1.0.0", "net.msrandom:class-extension-annotations:1.0.0"),
            )
        }

        fun put(
            name: String,
            text: String,
        ) {
            val p = directory.resolve(name)
            Files.createDirectories(p.parent)
            Files.writeString(p, text)
        }
        for (name in listOf("settings.gradle", "build.gradle", "gradle.properties", "artifact-identity.gradle")) {
            Files.copy(Path.of("integration-test/mixin-compilation/$name"), directory.resolve(name))
        }
        val metadata = Properties().apply { File(System.getProperty("fixture.metadata")).inputStream().use(::load) }
        val paths = metadata.getProperty("implementation-classpath").split(File.pathSeparator).map(::File)
        val original = paths.single { it.name.startsWith("minecraft-codev-mixins-") }
        assertTrue(original.isFile)
        val getter = net.msrandom.minecraftcodev.mixins.StripMixins.Parameters::class.java.getMethod("getRemovalsFile")
        assertEquals(org.gradle.api.file.ConfigurableFileCollection::class.java, getter.returnType)
        for (name in listOf(
            "net.msrandom.minecraftcodev.mixins.task.Mixin",
            "net.msrandom.minecraftcodev.mixins.StripMixins",
            "net.msrandom.minecraftcodev.mixins.ProduceMixinsKt",
            "net.msrandom.minecraftcodev.mixins.MixinRuntimeFailure",
        )) {
            val loaded =
                File(
                    Class
                        .forName(name)
                        .protectionDomain.codeSource.location
                        .toURI(),
                )
            assertArrayEquals(Files.readAllBytes(original.toPath()), Files.readAllBytes(loaded.toPath()), name)
            println("HOST_CODE_SOURCE $name $loaded")
        }
        Files.copy(Path.of(System.getProperty("fixture.metadata")), directory.resolve("classpath.properties"))
        val compilerPaths =
            paths.filter { it.isFile } +
                File(
                    org.gradle.api.file.ConfigurableFileCollection::class.java.protectionDomain.codeSource.location
                        .toURI(),
                )
        val compilerClasspath = compilerPaths.joinToString(File.pathSeparator) { it.path }

        fun compile(
            label: String,
            sources: Map<String, String>,
        ): Path {
            val output = directory.resolve("input/classes-$label")
            Files.createDirectories(output)
            val files =
                sources.map { (name, text) ->
                    val path = "input/src-$label/$name"
                    put(path, text)
                    directory.resolve(path).toString()
                }
            assertEquals(
                0,
                ToolProvider.getSystemJavaCompiler().run(
                    null,
                    null,
                    null,
                    "--release",
                    "17",
                    "-proc:none",
                    "-classpath",
                    compilerClasspath,
                    "-d",
                    output.toString(),
                    *files.toTypedArray(),
                ),
            )
            return output
        }

        fun jar(
            path: String,
            classes: Path?,
            resources: Map<String, String>,
        ) {
            val destination = directory.resolve(path)
            Files.createDirectories(destination.parent)
            JarOutputStream(Files.newOutputStream(destination)).use { jar ->
                if (classes != null) {
                    Files.walk(classes).use { files ->
                        files.filter(Files::isRegularFile).sorted().forEach { file ->
                            jar.putNextEntry(JarEntry(classes.relativize(file).toString().replace('\\', '/')))
                            Files.copy(file, jar)
                            jar.closeEntry()
                        }
                    }
                }
                resources.forEach { (name, value) ->
                    jar.putNextEntry(JarEntry(name))
                    jar.write(value.toByteArray())
                    jar.closeEntry()
                }
            }
        }
        if (scenario == "stub-from") {
            put("src/shared/java/fixture/SharedOnly.java", "package fixture; public class SharedOnly {}")
            put("src/right/java/fixture/ForgeOnly.java", "package fixture; public class ForgeOnly {}")
            for (version in 1..2) {
                val extra = if (version == 2) "public void preview() {}" else ""
                val classes = compile("widget-$version", mapOf("probe/Widget.java" to "package probe; public class Widget { public void open() {} $extra }"))
                val base = "repository/probe/widget/$version/widget-$version"
                jar("$base.jar", classes, emptyMap())
                put("$base.pom", "<project><modelVersion>4.0.0</modelVersion><groupId>probe</groupId><artifactId>widget</artifactId><version>$version</version></project>")
            }
        }
        val apiProbe =
            compile(
                "api",
                mapOf(
                    "ApiProbe.java" to
                        "public class ApiProbe { public static org.gradle.api.file.ConfigurableFileCollection records(net.msrandom.minecraftcodev.mixins.StripMixins.Parameters p) { return p.getRemovalsFile(); } }",
                ),
            )
        java.net.URLClassLoader((listOf(apiProbe.toFile()) + compilerPaths).map { it.toURI().toURL() }.toTypedArray(), null).use { loader ->
            val parameters = loader.loadClass("net.msrandom.minecraftcodev.mixins.StripMixins\$Parameters")
            assertEquals("org.gradle.api.file.ConfigurableFileCollection", parameters.getMethod("getRemovalsFile").returnType.name)
            assertEquals(
                original.canonicalFile,
                File(
                    parameters.protectionDomain.codeSource.location
                        .toURI(),
                ).canonicalFile,
            )
            loader.loadClass("ApiProbe").getMethod("records", parameters)
            println("JAVA_COMPILER_API_SOURCE $original")
        }
        val direct = scenario == "direct"
        val pkg = if (direct) "named" else "obf"
        val a = if (direct) "namedA" else "a"
        val b = if (direct) "namedB" else "b"
        val c = if (direct) "namedC" else "c"
        val common =
            compile(
                "common",
                mapOf(
                    "$pkg/Game.java" to "package $pkg; class Game { private int $a() { return 11; } private int $b() { return 22; } }",
                    "shared/Duplicate.java" to
                        "package shared; public class Duplicate { public static String side() { return \"common\"; } }",
                ),
            )
        val client =
            compile(
                "client",
                mapOf(
                    "$pkg/ClientGame.java" to "package $pkg; public class ClientGame { private int $c() { return 33; } }",
                    "shared/Duplicate.java" to
                        "package shared; public class Duplicate { public static String side() { return \"client\"; } }",
                ),
            )
        jar(
            "input/client-library.jar",
            compile("client-library", mapOf("library/ClientSupport.java" to "package library; public class ClientSupport {}")),
            emptyMap(),
        )
        jar(
            "input/common.jar",
            common,
            mapOf(
                "common-only.txt" to "common",
                "duplicate.txt" to "common",
                "META-INF/MANIFEST.MF" to "Manifest-Version: 1.0\r\nChosen: common\r\n\r\n",
                "ref.json" to """{"package":"probe","mixins":["Ref","Unapplied"]}""",
            ),
        )
        jar(
            "input/client.jar",
            client,
            mapOf(
                "client-only.txt" to "client",
                "duplicate.txt" to "client",
                "META-INF/MANIFEST.MF" to "Manifest-Version: 1.0\r\nChosen: client\r\n\r\n",
            ),
        )
        put("input/game.accesswidener", "accessWidener v2 named\naccessible class named/Game\n")
        put("input/auxiliary.txt", "non-Mixin tool resource")
        put("input/own.json", """{"required":true,"package":"own","mixins":["MustNotApply"]}""")
        put(
            "input/mappings.tiny",
            "tiny\t2\t0\tobf\tintermediary\tnamed\nc\tobf/Game\tobf/Game\tnamed/Game\n\tm\t()I\ta\ta\tnamedA\n\tm\t()I\tb\tb\tnamedB\nc\tobf/ClientGame\tobf/ClientGame\tnamed/ClientGame\n\tm\t()I\tc\tc\tnamedC\n",
        )

        fun module(
            name: String,
            sources: Map<String, String>,
            config: String?,
            extra: Map<String, String> = emptyMap(),
        ) {
            val classes = if (sources.isEmpty()) null else compile(name, sources)
            val resources = linkedMapOf("keep.txt" to name)
            if (config != null) {
                val nested = if (name == "plain") ",\"jars\":[{\"file\":\"META-INF/jars/child.jar\"}]" else ""
                resources["fabric.mod.json"] =
                    """{"schemaVersion":1,"id":"$name","version":"1","mixins":["$name.json"]$nested}"""
                if (name == "plain") resources["META-INF/jars/child.jar"] = "nested input must not be a source"
                resources["$name.json"] = config
            }
            resources.putAll(extra)
            val moduleGroup = if (name == "ordinary") "ordinary" else "probe"
            val artifactName = if (name == "ordinary") "ref" else name
            val location = "repository/$moduleGroup/$artifactName/1/$artifactName-1"
            jar("$location.jar", classes, resources)
            put(
                "$location.pom",
                "<project><modelVersion>4.0.0</modelVersion><groupId>$moduleGroup</groupId><artifactId>$artifactName</artifactId><version>1</version>${if (name == "plain") "<dependencies><dependency><groupId>probe</groupId><artifactId>transitive</artifactId><version>1</version></dependency></dependencies>" else ""}</project>",
            )
        }
        val selector = if (direct) "namedA" else "alias"
        val refConfig = """{"required":true,"package":"probe","compatibilityLevel":"JAVA_17","mixins":["Ref"],"server":["Unapplied"]${if (direct) "" else ",\"refmap\":\"root-refmap.json\""}}"""
        module(
            "ref",
            mapOf(
                "probe/Ref.java" to
                    "package probe; @org.spongepowered.asm.mixin.Mixin(targets=\"$pkg.Game\") public interface Ref { @org.spongepowered.asm.mixin.gen.Invoker(\"$selector\") int callRef(); }",
                "probe/Unapplied.java" to
                    "package probe; @org.spongepowered.asm.mixin.Mixin(targets=\"$pkg.Game\") public interface Unapplied { @org.spongepowered.asm.mixin.gen.Invoker(\"$b\") int callServer(); }",
            ),
            refConfig,
            if (direct) emptyMap() else mapOf("root-refmap.json" to """{"mappings":{"probe/Ref":{"alias":"Lobf/Game;a()I"}}}"""),
        )
        module(
            "plain",
            mapOf(
                "probe/Plain.java" to
                    "package probe; @org.spongepowered.asm.mixin.Mixin(targets=\"$pkg.Game\") public interface Plain { @org.spongepowered.asm.mixin.gen.Invoker(\"$b\") int callPlain(); }",
                "probe/Client.java" to
                    "package probe; @org.spongepowered.asm.mixin.Mixin(targets=\"$pkg.ClientGame\") public interface Client { @org.spongepowered.asm.mixin.gen.Invoker(\"$c\") int callClient(); }",
            ),
            """{"required":true,"package":"probe","compatibilityLevel":"JAVA_17","mixins":[],"client":["Plain","Client"]}""",
        )
        if (scenario.startsWith("stub-")) {
            for (name in listOf("Left", "Right")) {
                module(
                    name.lowercase(),
                    mapOf(
                        "unique/$name.java" to
                            "package unique; @org.spongepowered.asm.mixin.Mixin(targets=\"obf.Game\") public interface $name { @org.spongepowered.asm.mixin.gen.Invoker(\"b\") int call$name(); }",
                    ),
                    """{"required":true,"package":"unique","compatibilityLevel":"JAVA_17","mixins":["$name"]}""",
                )
            }
        }
        module("transitive", emptyMap(), """{"required":false,"package":"transitive","mixins":[]}""")
        module("ordinary", emptyMap(), null, mapOf("ref.json" to refConfig))
        module(
            "runtime",
            if (classifier) {
                mapOf(
                    "runtime/Runtime.java" to
                        "package runtime; @org.spongepowered.asm.mixin.Mixin(targets=\"$pkg.Game\") public class Runtime { public int callRuntime() { return 77; } }",
                )
            } else {
                emptyMap()
            },
            if (classifier) """{"required":true,"package":"runtime","mixins":["Runtime"]}""" else """{"required":false,"package":"runtime","mixins":[]}""",
        )
        if (classifier) {
            jar(
                "repository/probe/ref/1/ref-1-extra.jar",
                compile(
                    "extra",
                    mapOf("extra/Extra.java" to "package extra; public class Extra { public static int value() { return 44; } }"),
                ),
                mapOf(
                    "fabric.mod.json" to """{"schemaVersion":1,"id":"extra","version":"1","mixins":["ref.json"]}""",
                    "ref.json" to refConfig,
                    "keep.txt" to "ordinary extra",
                ),
            )
            put("input/RuntimeNegative.java", "public class RuntimeNegative { int use(named.Game g) { return g.callRuntime(); } }")
        }
        put("populated/src/main/java/Populated.java", "public class Populated {}")
        Files.createDirectories(directory.resolve("empty"))
        val on = scenario != "off"
        put(
            "src/main/java/Consume.java",
            (if (on) "public class Consume { probe.Ref ref(named.Game g) { return g; } probe.Plain plain(named.Game g) { return g; } probe.Client client(named.ClientGame g) { return g; } Populated p;" else "public class Consume { Populated p;") +
                (
                    if (classifier) {
                        " int extra() { return extra.Extra.value(); }" +
                            (if (on) " int injected(named.Game g) { probe.Ref ref = g; return ref.callRef(); } int injectedClient(named.ClientGame g) { probe.Client client = g; return client.callClient(); }" else "")
                    } else {
                        ""
                    }
                ) +
                " }",
        )
        put(
            "src/main/kotlin/ConsumeKotlin.kt",
            (if (on) "fun ref(g: named.Game): probe.Ref = g\nfun client(g: named.ClientGame): probe.Client = g\n" else "class ConsumeKotlin(val p: Populated)\n") +
                (
                    if (classifier) {
                        "fun extraValue(): Int = extra.Extra.value()\n" +
                            (if (on) "fun injected(g: named.Game): Int { val ref: probe.Ref = g; return ref.callRef() }\nfun injectedClient(g: named.ClientGame): Int { val client: probe.Client = g; return client.callClient() }\n" else "")
                    } else {
                        ""
                    }
                ),
        )
        val cache = home.resolve("caches/minecraft-codev")
        Files.createDirectories(cache.resolve("version-metadata"))
        val versions = if (scenario.startsWith("stub-")) listOf("1.20.1", "1.20.2") else listOf(if (direct) "26.1" else "1.20.1")
        Files.writeString(
            cache.resolve("version-manifest.json"),
            """{"latest":{},"versions":[${versions.joinToString(
                ",",
            ) { version ->
                """{"id":"$version","type":"release","url":"https://invalid.local/version","sha1":null,"time":"2026-01-01T00:00:00Z","releaseTime":"2026-01-01T00:00:00Z"}"""
            }}]}""",
        )
        for (version in versions) {
            Files.writeString(
                cache.resolve("version-metadata/$version.json"),
                """{"assetIndex":{"id":"fixture","sha1":"","size":0,"totalSize":0,"url":"https://invalid.local/assets"},"assets":"fixture","downloads":{},"id":"$version","javaVersion":{"majorVersion":17},"libraries":[],"type":"release","mainClass":"fixture.Main"}""",
            )
        }
        println("FIXTURE_PREPARED $directory nanos=${System.nanoTime() - prepareStarted}")
    }
}
