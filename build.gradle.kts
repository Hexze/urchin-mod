import dev.detekt.gradle.Detekt
import net.ltgt.gradle.errorprone.errorprone
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.20"
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("ploceus") version "1.17.4" apply false
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("net.ltgt.errorprone") version "5.1.1"
}

val isOrnithe = stonecutter.current.version == "1.8.9"
val ploceus = if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")

    configurations.configureEach {
        exclude(group = "org.lwjgl.lwjgl")
    }

    extensions.getByType<PloceusGradleExtensionApi>().apply {
        setIntermediaryGeneration(2)
    }
} else {
    null
}

val modid: String = sc.properties["mod.id"]
val modname: String = sc.properties["mod.name"]
val modversion: String = sc.properties["mod.version"]
val mcversion: String = sc.current.version
val versionrange: String = sc.properties["mod.mc_compat"]
val loaderversion: String = sc.properties["deps.fabric_loader"]
val oneconfigversion: String = sc.properties["deps.oneconfig"]
val loader = if (isOrnithe) "ornithe" else "fabric"
val requiredJava = JavaVersion.VERSION_25

version = "$modversion+$mcversion"
group = sc.properties["mod.group"] as String
base.archivesName = modid

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenCentral()
    google()
    maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
    maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        name = "Sonatype Snapshots"
        content { includeGroup("net.kyori") }
    }
    maven("https://maven.cloverclient.com/releases") {
        content { includeGroup("pl.tomgirl") }
    }
    strictMaven(if (isOrnithe) "https://maven.ornithemc.net/releases" else "https://maven.terraformersmc.com/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.pkg.jetbrains.space/public/p/compose/dev", "JetBrains Compose Dev", "org.jetbrains.skiko")
    strictMaven("https://maven.fabricmc.net/", "FabricMC", "net.fabricmc")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcversion")
    if (isOrnithe) {
        mappings(ploceus!!.layeredMappings {
            mappings("net.ornithemc:feather-gen2:$mcversion+build.${sc.properties["feather_build"] as String}:v2") {
                containsUnpick()
            }
            mappings(rootProject.file("mappings/feather-overrides.tiny"))
        })
    } else {
        loomx.applyMojangMappings()
    }

    modImplementation("net.fabricmc:fabric-loader:$loaderversion")
    modImplementation("org.polyfrost.oneconfig:$mcversion-$loader:$oneconfigversion")
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "utils")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigversion")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${sc.properties["deps.kotlinx_serialization"] as String}")
    if (isOrnithe) {
        for (module in arrayOf("lwjgl", "lwjgl-opengl")) {
            compileOnly("org.lwjgl:$module:${sc.properties["deps.lwjgl"] as String}")
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${sc.properties["deps.junit"] as String}")
    testImplementation("net.fabricmc:fabric-loader-junit:$loaderversion")

    errorprone("com.google.errorprone:error_prone_core:${sc.properties["deps.errorprone"] as String}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
    options.errorprone {
        disableAllChecks = true
        error("UnnecessarilyFullyQualified")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget = JvmTarget.fromTarget(requiredJava.majorVersion)
}

tasks.named<Detekt>("detekt") {
    enabled = false
    dependsOn(tasks.withType<Detekt>().matching { it.name != "detekt" && !it.name.endsWith("SourceSet") })
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
    }

    runConfigs.remove(runConfigs["server"])
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

bloom {
    replacement("@MOD_ID@", modid)
    replacement("@MOD_NAME@", modname)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    processResources {
        val props = mapOf(
            "mod_id" to modid,
            "mod_name" to modname,
            "mod_version" to modversion,
            "minecraft_version_range" to versionrange,
            "loader_version" to loaderversion,
            "oneconfig_version" to oneconfigversion,
        )

        inputs.properties(props)

        filesMatching("fabric.mod.json") { expand(props) }
    }
}
