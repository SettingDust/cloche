rootProject.name = "project-context"
include("subject", "producer")
listOf("subject", "producer").forEach { file(it).mkdirs() }
