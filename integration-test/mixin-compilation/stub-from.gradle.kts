import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.ClochePlugin

buildscript {
    val metadata = java.util.Properties().apply {
        file(providers.gradleProperty("clocheClasspathMetadata").get()).inputStream().use { load(it) }
    }
    dependencies { classpath(files(metadata.getProperty("implementation-classpath").split(File.pathSeparator))) }
}

pluginManager.apply(ClochePlugin::class.java)
group = "probe"
version = "1"
configure<ClocheExtension> {
    metadata.modId.set("stub_from_probe")
    val shared = common("shared")
    val control = common("control")
    val fabric = fabric("left") {
        minecraftVersion.set("1.20.2")
        loaderVersion.set("0.16.0")
        mixinPreapplication.set(false)
        dependsOn(shared, control)
        dependencies { implementation.add("probe:widget:2") }
        data { dependencies { implementation.add("probe:widget:2") } }
        client {
            dependencies { implementation.add("probe:widget:2") }
            test { dependencies { implementation.add("probe:widget:2") } }
        }
    }
    val forge = forge("right") {
        minecraftVersion.set("1.20.1")
        loaderVersion.set("47.4.20")
        mixinPreapplication.set(false)
        dependencies { compileOnly.add("probe:widget:1") }
        data { dependencies { compileOnly.add("probe:widget:1") } }
    }
    shared.data {}
    shared.test {}
    shared.client { data {}; test {} }
    when (providers.gradleProperty("mode").get()) {
        "explicit" -> shared.stubFrom(forge)
        "duplicates" -> { shared.stubFrom(forge); shared.stubFrom(forge); shared.stubFrom(fabric) }
    }
}
apply(from = "observe.gradle")
