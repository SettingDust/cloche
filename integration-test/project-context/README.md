# Project context regression smoke

From the repository root, first build `:pluginUnderTestMetadata`. Then run the fixture with the same Gradle user home:

```sh
./gradlew :pluginUnderTestMetadata --gradle-user-home <gradle-user-home>
./gradlew -p integration-test/project-context verify --no-configuration-cache --gradle-user-home <gradle-user-home>
```

The fixture reads the generated plugin classpath. It applies Cloche to separate projects and checks parent/child common main, client, data and client-data configuration: source links, common buckets, deferred API dependencies and constraints, the owning Project, API-stub task producers, and exclusion of project artifacts during sync. It evaluates providers without running Minecraft, remap or compiler-plugin tasks.
