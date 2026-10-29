plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "cl.duoc.pizzeria"
version = "1.0"

repositories { mavenCentral() }

kotlin {
    // Sin jvmToolchain: usa el JDK con que se abre el proyecto (17 o superior)
    // El código está en src/ (estructura de proyecto IntelliJ), no en src/main/kotlin
    sourceSets["main"].kotlin.srcDir("src")
}

application { mainClass.set("MainKt") }

// Permite leer desde consola con readLine() al ejecutar `gradle run`
tasks.named<JavaExec>("run") { standardInput = System.`in` }
