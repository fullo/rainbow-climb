val gdxVersion: String by project
val kotlinVersion: String by project

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

application {
    mainClass.set("com.darumahq.rainbowclimb.DesktopLauncherKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}
