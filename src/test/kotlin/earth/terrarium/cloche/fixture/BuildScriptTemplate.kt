package earth.terrarium.cloche.fixture

object BuildScriptTemplate {
    fun render(
        spec: LoaderScenarioSpec,
        verificationTask: String,
    ): String =
        """
        ${spec.buildScriptPreamble}

        plugins {
${spec.additionalPluginLines.prependIndent("            ")}
            id("earth.terrarium.cloche")
        }

        group = "${spec.group}"
        version = "${spec.version}"

        repositories {
${RepositoryPresets.render(spec.repositories)}
        }

        cloche {
            minecraftVersion = "${spec.minecraftVersion}"

            metadata {
                modId = "${spec.modId}"
            }

            mappings {
${spec.mappingsBlock.prependIndent("                ")}
            }

            ${spec.loaderKind.blockName} {
                loaderVersion = "${spec.loaderVersion}"

                dependencies {
${spec.dependenciesBlock.prependIndent("                    ")}
                }
            }
        }

${verificationTask.prependIndent("        ")}
        """.trimIndent()
}
