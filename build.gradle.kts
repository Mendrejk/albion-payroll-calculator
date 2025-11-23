plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

composeCompiler {
    // Only enable for desktop target
    targetKotlinPlatforms.set(setOf(org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm))
}

group = "me.user"
version = "2.0.0"

repositories {
    mavenCentral()
    google()
}

kotlin {
    // Native targets (CLI)
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
                runTask?.standardInput = System.`in`
            }
        }
    }

    // JVM target (Desktop GUI)
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    sourceSets {
        // Shared business logic
        commonMain.dependencies {
            implementation(libs.kotlinxSerializationJson)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
        }

        // Shared tests
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // Native CLI dependencies
        nativeMain.dependencies {
            implementation("com.squareup.okio:okio:3.9.1")
        }

        // Desktop GUI dependencies
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        
        nativeDistributions {
            // Use Msi for installer, but we'll use the app directory for portable
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "AlbionPayrollCalculator"
            packageVersion = "2.0.0"
            description = "Albion Online Payroll Calculator - GUI Edition"
            vendor = "Albion Payroll"
            
            windows {
                perUserInstall = false
                menuGroup = "Albion Payroll"
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }
        }
    }
}

// Task to create a truly portable distribution
tasks.register<Copy>("createPortable") {
    dependsOn("createDistributable")
    from(layout.buildDirectory.dir("compose/binaries/main/app"))
    into(layout.buildDirectory.dir("compose/portable"))
    
    doLast {
        println("Portable distribution created at: ${layout.buildDirectory.get()}/compose/portable")
        println("Run AlbionPayrollCalculator/AlbionPayrollCalculator.exe to start")
    }
}

// Task to create a portable ZIP for distribution
tasks.register<Zip>("packagePortableZip") {
    dependsOn("createPortable")
    from(layout.buildDirectory.dir("compose/portable"))
    archiveFileName.set("AlbionPayrollCalculator-${version}-portable.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    
    doLast {
        println("Portable ZIP created at: ${archiveFile.get()}")
        println("Extract and run AlbionPayrollCalculator/AlbionPayrollCalculator.exe")
    }
}
