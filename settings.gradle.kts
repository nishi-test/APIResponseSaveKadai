rootProject.name = "kadai"

// デバッグログを有効にする
gradle.rootProject {
    tasks.withType<org.gradle.api.tasks.compile.JavaCompile> {
        options.isDebug = true
    }
}
