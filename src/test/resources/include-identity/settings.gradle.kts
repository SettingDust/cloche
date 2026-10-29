rootProject.name = "include-identity-fixture"
include("producer", "single", "consumer", "library")
if (providers.gradleProperty("fixtureScenario").orNull == "collision") include(":other:producer")
