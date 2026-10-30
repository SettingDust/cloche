package earth.terrarium.cloche

import kotlinx.serialization.json.*
import net.msrandom.minecraftcodev.core.MinecraftCodevPlugin
import net.msrandom.minecraftcodev.includes.IncludeTargetOrigin
import net.msrandom.minecraftcodev.includes.IncludeTargetSource
import net.msrandom.minecraftcodev.includes.IncludedJarInfo
import net.msrandom.minecraftcodev.includes.IncludesJar
import net.msrandom.minecraftcodev.includes.ResolvedIncludeSource
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipInputStream

class IncludeTargetIdentityTests {
    private val reports = Path.of("build/reports/test/include-identity").toAbsolutePath()
    private val source = Path.of("integration-test/include-identity")
    private val modules = mapOf("string-lib" to "1.2", "provider-lib" to "3.4", "indirect" to "9.9", "existing-lib" to "4.5")

    @Test
    fun targetsAndOrdinaryCollectorCallsDeliverRealJarJar() = assertJarJar("positive")

    @Test
    fun singleTargetAndOrdinaryCollectorCallsDeliverRealJarJar() = assertJarJar("single")

    @Test
    fun inheritedTargetsUseReceivingCompilationAttributesWithoutDuplicates() {
        val (directory, result) = fixture("inheritance")
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:includeJar")?.outcome)
        listOf("data", "client", "clientData").forEach { name ->
            assertTrue(result.output.contains("INHERITED $name TARGET_GRAPHS 3"))
        }
        assertTrue(result.output.contains("INCLUDE_GRAPH clientDataIncludeTarget0Files"))
        assertTrue(result.output.contains("earth.terrarium.cloche.data=true"))
        assertTrue(result.output.contains("io.github.mcgradleconventions.distribution=client"))
        assertTrue(result.output.contains("SNAPSHOT_STABLE inheritance"))
        assertTrue(result.output.contains("ORDINARY_ACTION_CALLS 1"))
        val jar = zip(Files.readAllBytes(directory.resolve("build/included/fabric.jar")))
        val metadata = Json.parseToJsonElement(jar.getValue("fabric.mod.json").decodeToString()).jsonObject
        assertEquals(6, metadata.getValue("jars").jsonArray.size)
        listOf("a", "b", "single").forEach { label ->
            val nested = zip(jar.getValue("META-INF/jars/$label.jar"))
            assertArrayEquals(Files.readAllBytes(source.resolve("payloads/$label.txt")), nested.getValue("payload.txt"))
            val embedded = Json.parseToJsonElement(nested.getValue("fabric.mod.json").decodeToString()).jsonObject
            assertEquals(if (label == "single") "late_single_single" else "late_targets_producer-demo-$label", embedded.getValue("id").jsonPrimitive.content)
            assertEquals(if (label == "single") "6.7" else "5.6", embedded.getValue("version").jsonPrimitive.content)
        }
        Files.write(directory.resolve("fabric.mod.json"), jar.getValue("fabric.mod.json"))
    }

    private fun assertJarJar(scenario: String) {
        val withB = scenario != "single"
        val (directory, result) = fixture(scenario)
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:includeJar")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":producer:fixtureA")?.outcome)
        if (withB) assertEquals(TaskOutcome.SUCCESS, result.task(":producer:fixtureB")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":single:fixtureSINGLE")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":library:jar")?.outcome)
        assertTrue(result.output.contains("CONFIGURATION_UNRESOLVED production include registration TARGET_GRAPHS ${if (withB) 3 else 2}"))
        assertTrue(result.output.contains("SNAPSHOT_STABLE $scenario"))
        if (scenario == "inheritance") {
            assertTrue(result.output.contains("INHERITED data TARGET_GRAPHS 3"))
            assertTrue(result.output.contains("INHERITED test TARGET_GRAPHS 3"))
            assertTrue(result.output.contains("INCLUDE_GRAPH dataIncludeTarget0Files"))
            assertTrue(result.output.contains("earth.terrarium.cloche.data=true"))
        }
        assertTrue(result.output.contains("REQUEST :producer late.targets:producer-demo-a"))
        if (withB) assertTrue(result.output.contains("REQUEST :producer late.targets:producer-demo-b"))
        assertTrue(result.output.contains("REQUEST :single late.single:single"))
        assertTrue(result.output.contains("SELECTED project :producer VARIANT demoARuntimeElements"))
        if (withB) assertTrue(result.output.contains("SELECTED project :producer VARIANT demoBRuntimeElements"))
        assertTrue(result.output.contains("TRANSITIVE false"))
        val jar = zip(Files.readAllBytes(directory.resolve("build/included/forge.jar")))
        val metadata = Json.parseToJsonElement(jar.getValue("META-INF/jarjar/metadata.json").decodeToString()).jsonObject
        val entries = metadata.getValue("jars").jsonArray
        assertEquals(if (withB) 6 else 5, entries.size)
        val expected =
            mapOf(
                "late.targets:producer-demo-a" to Triple("5.6", "a.jar", "target A\n"),
                "late.targets:producer-demo-b" to Triple("5.6", "b.jar", "target B\n"),
                "late.single:single" to Triple("6.7", "single.jar", "single target\n"),
                "ordinary.lib:library" to Triple("2.3", "library-2.3.jar", "ordinary project\n"),
                "org.fixture:string-lib" to Triple("1.2", "string-lib-1.2.jar", "string module\n"),
                "org.fixture:provider-lib" to Triple("3.4", "provider-lib-3.4.jar", "provider module\n"),
            ).filterKeys { withB || it != "late.targets:producer-demo-b" }
        val seen =
            entries.map { element ->
                val entry = element.jsonObject
                assertEquals(setOf("identifier", "version", "path"), entry.keys)
                val identifier = entry.getValue("identifier").jsonObject
                val identity =
                    identifier.getValue("group").jsonPrimitive.content + ":" + identifier.getValue("artifact").jsonPrimitive.content
                val (version, filename, payload) = expected.getValue(identity)
                val versions = entry.getValue("version").jsonObject
                assertEquals(setOf("range", "artifactVersion"), versions.keys)
                assertEquals(version, versions.getValue("artifactVersion").jsonPrimitive.content)
                val range = if (identity.startsWith("org.fixture:")) version else "[$version,)"
                assertEquals(range, versions.getValue("range").jsonPrimitive.content)
                val path = entry.getValue("path").jsonPrimitive.content
                assertEquals("META-INF/jars/$filename", path)
                assertEquals(payload, zip(jar.getValue(path)).getValue("payload.txt").decodeToString())
                identity
            }
        assertEquals(expected.keys, seen.toSet())
        assertEquals(expected.size, jar.keys.count { it.startsWith("META-INF/jars/") && it.endsWith(".jar") })
        assertFalse(jar.keys.any { it.contains("indirect") })
        Files.write(directory.resolve("jarjar-metadata.json"), jar.getValue("META-INF/jarjar/metadata.json"))
    }

    @Test
    fun incompatibleMinecraftTargetFailsOriginalAttributeSelection() {
        val (_, result) = fixture("incompatible", fails = true)
        assertTrue(result.output.contains("No matching variant of project :producer was found"), result.output)
        assertTrue(result.output.contains("1.20.2") && result.output.contains("1.20.1"), result.output)
        assertTrue(result.output.contains("io.github.mcgradleconventions.version"), result.output)
    }

    @Test
    fun missingLateBaseCapabilityFailsNormalResolution() {
        val (_, result) = fixture("missing", fails = true)
        assertTrue(result.output.contains("EARLY_CAPABILITIES"), result.output)
        assertTrue(result.output.contains("late.targets:producer-demo-a"), result.output)
        assertTrue(result.output.contains("Unable to find a variant") && result.output.contains("requested capability"), result.output)
    }

    @Test
    fun fabricUsesDefaultAndExplicitIdentityAndPreservesExistingMetadata() {
        val (directory, result) = fixture("fabric")
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:includeJar")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":producer:fixtureA")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":producer:fixtureB")?.outcome)
        assertTrue(result.output.contains("INCLUDED org.fixture:string-lib:1.2"))
        assertTrue(result.output.contains("INCLUDED chosen.group:library-extra:7.6"))
        assertTrue(result.output.contains("SNAPSHOT_STABLE fabric"))
        val jar = zip(Files.readAllBytes(directory.resolve("build/included/fabric.jar")))
        val outer = Json.parseToJsonElement(jar.getValue("fabric.mod.json").decodeToString()).jsonObject
        assertEquals("outer_fixture", outer.getValue("id").jsonPrimitive.content)
        assertEquals(
            "outer",
            outer
                .getValue("custom")
                .jsonObject
                .getValue("keep")
                .jsonPrimitive.content,
        )
        val paths =
            outer
                .getValue("jars")
                .jsonArray
                .map {
                    it.jsonObject
                        .getValue("file")
                        .jsonPrimitive.content
                }.toSet()
        assertEquals(
            setOf(
                "META-INF/jars/a.jar",
                "META-INF/jars/b.jar",
                "META-INF/jars/string-lib-1.2.jar",
                "META-INF/jars/library-2.3.jar",
                "META-INF/jars/existing-lib-4.5.jar",
            ),
            paths,
        )
        listOf(
            Triple("string-lib-1.2.jar", "org_fixture_string-lib", "1.2"),
            Triple("library-2.3.jar", "chosen_group_library-extra", "7.6"),
        ).forEach { (filename, id, version) ->
            val nested = zip(jar.getValue("META-INF/jars/$filename"))
            val metadata = Json.parseToJsonElement(nested.getValue("fabric.mod.json").decodeToString()).jsonObject
            assertEquals(id, metadata.getValue("id").jsonPrimitive.content)
            assertEquals(version, metadata.getValue("version").jsonPrimitive.content)
            assertEquals(
                if (filename.startsWith("library")) "library-extra" else "string-lib",
                metadata.getValue("name").jsonPrimitive.content,
            )
            assertTrue(
                metadata
                    .getValue("custom")
                    .jsonObject
                    .getValue("fabric-loom:generated")
                    .jsonPrimitive.boolean,
            )
            assertEquals(
                if (filename.startsWith("library")) "ordinary project\n" else "string module\n",
                nested.getValue("payload.txt").decodeToString(),
            )
            Files.write(directory.resolve("$filename.fabric.mod.json"), nested.getValue("fabric.mod.json"))
        }
        listOf("a", "b").forEach { name ->
            val nested = zip(jar.getValue("META-INF/jars/$name.jar"))
            val metadata = Json.parseToJsonElement(nested.getValue("fabric.mod.json").decodeToString()).jsonObject
            assertEquals("late_targets_producer-demo-$name", metadata.getValue("id").jsonPrimitive.content)
            assertEquals("producer-demo-$name", metadata.getValue("name").jsonPrimitive.content)
            assertEquals("5.6", metadata.getValue("version").jsonPrimitive.content)
            assertEquals("target ${name.uppercase()}\n", nested.getValue("payload.txt").decodeToString())
            Files.write(directory.resolve("$name.jar.fabric.mod.json"), nested.getValue("fabric.mod.json"))
        }
        val existing = zip(jar.getValue("META-INF/jars/existing-lib-4.5.jar"))
        assertArrayEquals(Files.readAllBytes(source.resolve("metadata/existing/fabric.mod.json")), existing.getValue("fabric.mod.json"))
        assertEquals("existing Fabric library\n", existing.getValue("payload.txt").decodeToString())
        assertFalse(jar.keys.any { it.contains("indirect") })
        Files.write(directory.resolve("fabric.mod.json"), jar.getValue("fabric.mod.json"))
    }

    @Test
    fun ordinaryVersionsTakePrecedenceAcrossProductionTargetSources() {
        mapOf("strict" to "[5,6)", "required" to "[5,7)", "preferred" to "5.6", "project" to "[5.6,)", "default" to "[6.7,)").forEach { (kind, range) ->
            val (directory, result) = fixture("mixed-$kind")
            assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:includeJar")?.outcome)
            assertTrue(result.output.contains("ORDINARY_LAST mixed-$kind"))
            val jar = zip(Files.readAllBytes(directory.resolve("build/included/forge.jar")))
            val metadata = Json.parseToJsonElement(jar.getValue("META-INF/jarjar/metadata.json").decodeToString()).jsonObject
            val entries = metadata.getValue("jars").jsonArray
            assertEquals(if (kind == "default") 1 else 2, entries.size)
            entries.forEach { element ->
                val entry = element.jsonObject
                val id = entry.getValue("identifier").jsonObject
                val name = id.getValue("artifact").jsonPrimitive.content
                val version = if (kind == "default") "6.7" else "5.6"
                assertEquals(if (kind == "default") "late.single" else "late.targets", id.getValue("group").jsonPrimitive.content)
                assertEquals(
                    version,
                    entry
                        .getValue("version")
                        .jsonObject
                        .getValue("artifactVersion")
                        .jsonPrimitive.content,
                )
                assertEquals(
                    if (name.endsWith("-b")) "[5.6,)" else range,
                    entry
                        .getValue("version")
                        .jsonObject
                        .getValue("range")
                        .jsonPrimitive.content,
                )
                val label =
                    if (kind == "default") {
                        "single"
                    } else if (name.endsWith("-b")) {
                        "b"
                    } else {
                        "a"
                    }
                val path = entry.getValue("path").jsonPrimitive.content
                assertEquals("META-INF/jars/$label.jar", path)
                assertArrayEquals(
                    Files.readAllBytes(source.resolve("payloads/$label.txt")),
                    zip(jar.getValue(path)).getValue("payload.txt"),
                )
            }
            Files.write(directory.resolve("jarjar-metadata.json"), jar.getValue("META-INF/jarjar/metadata.json"))
        }
    }

    @Test
    fun conflictingOrdinaryAndGeneratedIdentitiesFailWithSourceDetails() {
        val (_, result) = fixture("ambiguous", fails = true)
        assertTrue(result.output.contains("Ambiguous capability identities"), result.output)
        listOf(
            "a.jar",
            "demoARuntimeElements",
            "early.targets:producer",
            "late.targets:producer-demo-a",
            "ordinary",
            "IncludeTargetOrigin",
            "targetName=demo:a",
        ).forEach {
            assertTrue(result.output.contains(it), "Missing diagnostic $it\n${result.output}")
        }
    }

    @Test
    fun normalizedTargetsFromDistinctProjectsAreNotSilentlyMerged() {
        val (_, result) = fixture("collision", fails = true)
        assertEquals(TaskOutcome.SUCCESS, result.task(":producer:fixtureA")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":other:producer:fixtureA")?.outcome)
        assertEquals(2, Regex("INCLUDED late.targets:producer-demo-a:5.6").findAll(result.output).count())
        assertTrue(result.output.contains("META-INF/jars/a.jar") && result.output.contains("duplicate"), result.output)
    }

    @Test
    fun ordinaryOnlyCollectorStillUsesProductionIncludeJar() {
        val (directory, result) = fixture("ordinary")
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:includeJar")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":library:jar")?.outcome)
        assertFalse(result.tasks.any { it.path.startsWith(":producer:") || it.path.startsWith(":single:") })
        assertTrue(result.output.contains("TARGET_GRAPHS 0"))
        assertTrue(result.output.contains("ORDINARY_ACTION_CALLS 1"))
        val jar = zip(Files.readAllBytes(directory.resolve("build/included/forge.jar")))
        val metadata = Json.parseToJsonElement(jar.getValue("META-INF/jarjar/metadata.json").decodeToString()).jsonObject
        val entries = metadata.getValue("jars").jsonArray
        assertEquals(3, entries.size)
        val expected = mapOf("ordinary.lib:library" to Triple("2.3", "library-2.3.jar", "library"), "org.fixture:string-lib" to Triple("1.2", "string-lib-1.2.jar", "string-lib"), "org.fixture:provider-lib" to Triple("3.4", "provider-lib-3.4.jar", "provider-lib"))
        assertEquals(expected.keys, entries.map { element ->
            val entry = element.jsonObject
            val id = entry.getValue("identifier").jsonObject
            val identity = id.getValue("group").jsonPrimitive.content + ":" + id.getValue("artifact").jsonPrimitive.content
            val (version, filename, label) = expected.getValue(identity)
            val versions = entry.getValue("version").jsonObject
            assertEquals(version, versions.getValue("artifactVersion").jsonPrimitive.content)
            assertEquals(if (label == "library") "[$version,)" else version, versions.getValue("range").jsonPrimitive.content)
            val path = entry.getValue("path").jsonPrimitive.content
            assertEquals("META-INF/jars/$filename", path)
            assertArrayEquals(Files.readAllBytes(source.resolve("payloads/$label.txt")), zip(jar.getValue(path)).getValue("payload.txt"))
            identity
        }.toSet())
        Files.write(directory.resolve("jarjar-metadata.json"), jar.getValue("META-INF/jarjar/metadata.json"))
    }

    private fun fixture(
        scenario: String,
        fails: Boolean = false,
    ): Pair<Path, BuildResult> {
        val localBuild =
            File(
                checkNotNull(System.getProperty("fixture.codevRoot")),
                "minecraft-codev-includes/build",
            ).canonicalFile.toPath()
        val codevRoot = File(checkNotNull(System.getProperty("fixture.codevRoot"))).canonicalFile.toPath()
        val sources = mutableListOf<String>()
        listOf(
            IncludedJarInfo::class.java,
            IncludesJar::class.java,
            IncludeTargetOrigin::class.java,
            IncludeTargetSource::class.java,
            ResolvedIncludeSource::class.java,
            MinecraftCodevPlugin::class.java,
            Class.forName("net.msrandom.minecraftcodev.annotations.UnsafeForClient"),
        ).forEach { type ->
            val origin =
                Path
                    .of(
                        type.protectionDomain.codeSource.location
                            .toURI(),
                    ).toRealPath()
            val expected =
                when (type) {
                    MinecraftCodevPlugin::class.java -> codevRoot.resolve("minecraft-codev-core/build")
                    Class.forName("net.msrandom.minecraftcodev.annotations.UnsafeForClient") -> codevRoot.resolve("minecraft-codev-core/side-annotations/build")
                    else -> localBuild
                }
            assertTrue(origin.startsWith(expected), "$type came from $origin")
            val resources = type.classLoader.getResources(type.name.replace('.', '/') + ".class").toList()
            assertEquals(1, resources.size, "Duplicate includes implementations: $resources")
            sources.add("CODE_SOURCE ${type.name} $origin RESOURCES ${resources.size}")
            println(sources.last())
        }
        Files.createDirectories(reports)
        Files.write(reports.resolve("outer-sources.txt"), sources)
        val directory = Files.createTempDirectory(Files.createDirectories(Path.of("build/tmp/include-identity")), "$scenario-")
        println("FIXTURE $scenario $directory")
        val paths =
            listOf("settings.gradle.kts", "build.gradle.kts", "metadata/outer/fabric.mod.json", "metadata/existing/fabric.mod.json") +
                listOf("a", "b", "single", "library", "string-lib", "provider-lib", "indirect", "existing-lib").map { "payloads/$it.txt" } +
                modules.map { (name, version) -> "repository/org/fixture/$name/$version/$name-$version.pom" }
        paths.forEach { path ->
            val destination = directory.resolve(path)
            Files.createDirectories(destination.parent)
            Files.copy(source.resolve(path), destination)
        }
        listOf("producer", "single", "consumer", "library", "other/producer").forEach { Files.createDirectories(directory.resolve(it)) }
        modules.forEach { (name, version) ->
            JarOutputStream(Files.newOutputStream(directory.resolve("repository/org/fixture/$name/$version/$name-$version.jar"))).use { jar ->
                jar.putNextEntry(JarEntry("payload.txt"))
                jar.write(Files.readAllBytes(source.resolve("payloads/$name.txt")))
                jar.closeEntry()
                if (name == "existing-lib") {
                    jar.putNextEntry(JarEntry("fabric.mod.json"))
                    jar.write(Files.readAllBytes(source.resolve("metadata/existing/fabric.mod.json")))
                    jar.closeEntry()
                }
            }
        }
        val home = File(checkNotNull(System.getProperty("fixture.gradleUserHome"))).canonicalFile
        assertEquals(home, File(checkNotNull(System.getProperty("org.gradle.testkit.dir"))).canonicalFile)
        val runner =
            GradleRunner
                .create()
                .withProjectDir(directory.toFile())
                .withPluginClasspath()
                .withTestKitDir(home)
        assertFalse(
            runner.pluginClasspath.any {
                it.name.matches(Regex("(minecraft-codev-includes|minecraft-codev-core|side-annotations)-.*\\.jar"))
            },
        )
        listOf(
            localBuild.resolve("classes/kotlin/main"),
            codevRoot.resolve("minecraft-codev-core/build/classes/kotlin/main"),
            codevRoot.resolve("minecraft-codev-core/side-annotations/build/classes/java/main"),
        ).forEach { expected ->
            assertTrue(runner.pluginClasspath.any { it.canonicalFile.toPath() == expected })
        }
        val arguments =
            mutableListOf(
                ":consumer:includeJar",
                "--stacktrace",
                "--max-workers=1",
                "--no-configuration-cache",
                "--gradle-user-home",
                home.path,
                "-Dorg.gradle.java.home=" + System.getProperty("fixture.gradleJava"),
                "-PfixtureScenario=$scenario",
                "-PlocalCodevRoot=$codevRoot",
            )
        System.getProperty("org.gradle.java.installations.paths")?.let { arguments.add("-Porg.gradle.java.installations.paths=$it") }
        try {
            val result = if (fails) runner.withArguments(arguments).buildAndFail() else runner.withArguments(arguments).build()
            Files.writeString(directory.resolve("gradle.log"), result.output)
            assertTrue(result.output.contains("CODE_SOURCE net.msrandom.minecraftcodev.includes.IncludedJarInfo"))
            assertTrue(result.output.contains("CODE_SOURCE net.msrandom.minecraftcodev.includes.IncludesJar"))
            assertFalse(
                result.tasks.any {
                    it.path.contains("remap", ignoreCase = true) || it.path.contains("finalJar", ignoreCase = true) ||
                        it.path.contains("resolvePatched")
                },
            )
            return directory to result
        } catch (failure: UnexpectedBuildFailure) {
            Files.writeString(directory.resolve("gradle.log"), failure.buildResult.output)
            throw failure
        }
    }

    private fun zip(bytes: ByteArray): Map<String, ByteArray> =
        buildMap {
            ZipInputStream(ByteArrayInputStream(bytes)).use { input ->
                while (true) {
                    val entry = input.nextEntry ?: break
                    if (!entry.isDirectory) put(entry.name, input.readAllBytes())
                }
            }
        }
}
