package earth.terrarium.cloche.fixture

object RepositoryPresets {
    val remapRouting = listOf(
        "mavenCentral()",
        "maven(\"https://libraries.minecraft.net/\")",
        "maven(\"https://maven.fabricmc.net/\")",
        "maven(\"https://maven.neoforged.net/releases\")",
        "maven(\"https://maven.msrandom.net/repository/root/\")",
        "maven(\"https://maven.msrandom.net/repository/cloche/\")",
        "maven(\"https://maven.minecraftforge.net/\")",
    )

    fun render(lines: List<String>): String = lines.joinToString("\n") { "    $it" }
}
