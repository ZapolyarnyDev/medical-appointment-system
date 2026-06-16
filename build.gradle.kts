plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
}

group = "io.github.zapolyarnydev"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    compileOnly(libs.lombok)
    compileOnly(libs.jetbrains.annotations)
    annotationProcessor(libs.lombok)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    options.release.set(25)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("gradle") {
        target("*.gradle.kts", "gradle/**/*.toml")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }

    format("misc") {
        target("*.md", "*.yml", "*.yaml", "*.properties", ".gitignore", ".gitattributes", ".editorconfig")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}
