# Cloche

A general-purpose Minecraft Gradle plugin for all sorts of use-cases.

## Fork-specific changes

This fork tracks [terrarium-earth/cloche](https://github.com/terrarium-earth/cloche) and carries several additions and fixes that improve IDE support, remapping flexibility, and target resolution.

> [!IMPORTANT]
> Fork releases and related `-dust.*` artifacts are published to:
> `https://raw.githubusercontent.com/settingdust/maven/main/repository/`

### Mixin ([cloche#65](https://github.com/terrarium-earth/cloche/pull/65))
- Build-time mixin application for the compile classpath, providing a better IDE and debugging experience

### Remapping & Mappings ([cloche#87](https://github.com/terrarium-earth/cloche/pull/87))
- Allow specifying a remap namespace per dependency
- Allow adding intermediary or MCP/Searge Minecraft dependencies
- Useful for mixing Fabric and Forge/NeoForge artifacts in the same target

For example, to use a Fabric mod on a Forge target:
```kt
cloche.forge.mappings {
    fabricIntermediary()
}

cloche.forge.dependencies {
    // Add Forge Minecraft with MCP/Searge mappings
    remapClasspath(forgeMinecraft("1.20.1", "47.4.4"))

    // Remap a Fabric mod from intermediary namespace
    modImplementation("maven.modrinth:surveyor:0.6.26+1.20") {
        attributes {
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INTERMEDIARY)
        }
    }
}
```

Or to use a mod mapped in MCP/Searge:
```kt
cloche.target.mappings {
    mcpSearge()
}

cloche.target.dependencies {
    remapClasspath(forgeMinecraft("1.20.1", "47.4.4"))

    modImplementation(catalog.dynamictrees.mc120.forge) {
        attributes {
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.SEARGE)
        }
    }
}
```

### Target Attribute ([cloche#130](https://github.com/terrarium-earth/cloche/pull/130))
- Fix incorrectly resolving other targets with the same Minecraft version and loader
- Add a dedicated Cloche target attribute for proper target disambiguation

### Build & Compilation
- Uses the [SettingDust/minecraft-codev](https://github.com/SettingDust/minecraft-codev) fork of codev
- Adds JVM version targeting for common compilations ([cloche#153](https://github.com/terrarium-earth/cloche/pull/153))

### jvm-multiplatform
- Parallel stub API creation ([jvm-multiplatform#20](https://github.com/terrarium-earth/jvm-multiplatform/pull/20))
- Method body stub support ([jvm-multiplatform#21](https://github.com/terrarium-earth/jvm-multiplatform/pull/21))

Cloche functions in terms of targets, a target can have any Minecraft version or mod loader setup that you compile to, all within the same project.

A plethora of easily configurable features, including but not limited to:
- Separated client source-set where possible
- Simple Data Generation
- Tests for all different source-sets and configurations
- Run configurations generated for various different cases
- Pre-applied mixins, allowing for a better debug experience (WIP)
- Mod metadata(`fabric.mod.json`, `neoforge.mods.toml`, etc) generated for all targets
- Multi-platform utilities when using multiple targets, such as Java @Expect/@Actual annotations and Kotlin multiplatform features
  - Part of the [jvm-multiplatform](https://github.com/MsRandom/jvm-multiplatform) tool suite

### Publishing and Consumption
If you publish a library/mod API with Cloche, variants are automatically configured for consumers, thus if you use the library in common, it will automatically pick the right variants for each consuming target.

## Setup

> [!IMPORTANT]
> If you are using this fork, add the custom Maven repository below in `settings.gradle` or `settings.gradle.kts` so Gradle can resolve the plugin and its forked dependencies:
>
> ```kt
> pluginManagement {
>     repositories {
>         gradlePluginPortal()
>         maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/")
>     }
> }
> ```

The basic structure for using Cloche in a `build.gradle`(`.kts`) build script is generally as follows:
```kt
plugins {
    id("earth.terrarium.cloche") version "VERSION"
}

// Group and version can be in gradle.properties as well
group = "net.xyz"
version = "1.0.0"

// Add the relevant repositories, depending on what targets you have
repositories {
  maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") // Required for this fork and forked codev artifacts

  cloche.librariesMinecraft() // libraries.minecraft.net, always recommended first as Mojang sometimes publishes non-standard classifiers there which are needed on certain platforms

  mavenCentral() // Maven central second for best reliability

  cloche {
    main() // General multiplatform or configuration libraries, generally not needed in single-target neoforge

    // Neoforge specific mavens (if neoforge targets are added)
    mavenNeoforgedMeta()
    mavenNeoforged(/* releases */)

    mavenFabric() // maven.fabricmc.net (if fabric targets are added)
    mavenForge() // maven.minecraftforge.net (if forge targets are added)

    mavenParchment() // maven.parchmentmc.org (if parchment is used)
  }
}

cloche {
    metadata {
        // Automatically generate mod metadata file
        modId = "modid"
        name = "Mod Name"
        description = "My Awesome Mod"
        license = "MIT"

        author("XYZ")
    }

    // (Target setup goes here)
}
```

You can then set up the targets in various different ways, for example:

### Neoforge 1.21.1
```kt
minecraftVersion = "1.21.1"

singleTarget {
    // Single target mode
    neoforge {
        loaderVersion = "21.1.26"
    }
}
```

### Source sets and run configurations
Within a target, you can configure data, tests and client source sets and runs(everything below is optional)
```kt
fabric {
    data()
    test()

    // For separate client sourceset
    client {
        data()
        test()
    }

    // otherwise
    includedClient()

    runs {
        // 6 available types of autoconfigured run configurations
        server()
        client()

        data()
        clientData()

        test()
        clientTest()
    }
}

neoforge {
    data()
    test()

    // no client configuration as forge-like targets always include client classes

    runs {
        // Same as above
    }
}
```

### Multi-loader
```kt
minecraftVersion = "1.21.1"

common {
    // common is implicit if not in single target mode, but can be additionally configured
    dependencies {
        implementation("some.module:my-library:1.0.0")
    }
}

neoforge {
    loaderVersion = "21.1.26"
}

fabric {
    loaderVersion = "0.16.2"

    dependencies {
        fabricApi("0.102.1") // Optional
    }
}
```

### Multi-version
```kt
// There can be multiple targets of different versions, with a common Jar generated with their common APIs
fabric("1.21.1") {
    minecraftVersion = "1.21.1"

    loaderVersion = "0.16.2"

    dependencies {
        fabricApi("0.102.1")
    }
}

fabric("1.19.4") {
    minecraftVersion = "1.19.4"

    loaderVersion = "0.14.19"

    dependencies {
        fabricApi("0.79.0")
    }
}
```

### Note on naming
When you have multiple combinations of mod loaders & minecraft versions(as is common for multi-version mods supporting both fabric and forge/neoforge),
you can use the `:` character to split the directory structure, ie `fabric:1.20.1` for classifier `fabric-1.20.1` and directory structure `src/fabric/1.20.1`

This could be expanded to any configuration of different loaders and versions.
