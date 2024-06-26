import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {

    implementation(project(":core"))
    implementation(compose.desktop.currentOs)

    // Skia Kotlin高性能图形绘制的库
    implementation("org.jetbrains.skiko:skiko-awt:0.7.93")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${rootProject.extra["kotlinVersion"]}")
    implementation("org.jetbrains.compose.material3:material3:${rootProject.extra["composeVersion"]}")


    // 脚本相关
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("org.bytedeco:opencv-platform:4.9.0-1.5.10")
    implementation("com.sikulix:sikulixapi:2.0.5")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.h2database:h2:${rootProject.extra["h2Version"]}")
    implementation("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")
    implementation("cn.hutool:hutool-all:${rootProject.extra["hutoolVersion"]}")
    implementation("org.apache.commons:commons-lang3:${rootProject.extra["commonsLangVersion"]}")
    implementation("org.slf4j:slf4j-api:${rootProject.extra["slf4jVersion"]}")
    implementation("ch.qos.logback:logback-classic:${rootProject.extra["logbackVersion"]}")

    implementation("com.baomidou:mybatis-plus-boot-starter:${rootProject.extra["mybatisPlusVersion"]}")
    implementation("com.baomidou:mybatis-plus-annotation:${rootProject.extra["mybatisPlusVersion"]}")
    implementation("com.github.pagehelper:pagehelper-spring-boot-starter:2.1.0")



    testImplementation("org.springframework.boot:spring-boot-starter-test:${rootProject.extra["springBootVersion"]}")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:${rootProject.extra["springBootVersion"]}")
    testImplementation("com.h2database:h2:${rootProject.extra["h2Version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.extra["junitVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra["junitVersion"]}")
    annotationProcessor("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "eDS"
            packageVersion = "1.0.0"


            // 设置程序的图标
            macOS {
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
            }

        }
    }
}

tasks.register<JavaExec>("runSpringBoot") {
    group = "application"
    description = "Run the Spring Boot application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.xtl.ebusiness.AutomationApplication")
}

springBoot {
    mainClass.set("com.xtl.ebusiness.AutomationApplication")
}
