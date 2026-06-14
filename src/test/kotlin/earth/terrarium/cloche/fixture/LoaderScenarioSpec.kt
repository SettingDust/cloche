package earth.terrarium.cloche.fixture

data class LoaderScenarioSpec(
    val projectName: String,
    val group: String = "test.routing",
    val version: String = "1.0.0",
    val minecraftVersion: String = "1.20.1",
    val modId: String = projectName,
    val buildScriptPreamble: String = "",
    val additionalPluginLines: String = "",
    val loaderKind: LoaderKind,
    val loaderVersion: String,
    val repositories: List<String>,
    val mappingsBlock: String,
    val dependenciesBlock: String,
)
