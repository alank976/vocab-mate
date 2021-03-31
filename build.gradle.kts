plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    id("org.jetbrains.kotlin.kapt") version "1.4.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("io.micronaut.application") version "1.4.2"
    id("com.expediagroup.graphql") version "4.0.0-alpha.17"
}

version = "0.1"
group = "github.io"

val kotlinVersion: String by project
val kotestVersion: String by project
val expediaGraphqlClientVersion: String by project
val ktorVersion: String by project
val faunaApiKey: String by project
repositories {
    mavenCentral()
}

micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("github.io.*")
    }
}

dependencies {
    kapt("io.micronaut.jaxrs:micronaut-jaxrs-processor")
    kapt("io.micronaut.openapi:micronaut-openapi")
    implementation("io.swagger.core.v3:swagger-annotations")
    kaptTest("io.micronaut.jaxrs:micronaut-jaxrs-processor")
    compileOnly("org.graalvm.nativeimage:svm")
    implementation("io.micronaut:micronaut-validation")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("javax.annotation:javax.annotation-api")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.graphql:micronaut-graphql")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.rxjava3:micronaut-rxjava3")
    implementation("io.micronaut.jaxrs:micronaut-jaxrs-server")
//    graphQL codegen
    implementation("com.expediagroup:graphql-kotlin-client:$expediaGraphqlClientVersion")
    implementation("com.expediagroup:graphql-kotlin-spring-client:$expediaGraphqlClientVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$expediaGraphqlClientVersion")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}


application {
    mainClass.set("github.io.vocabmate.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

val graphqlGeneratedClientPackage = "github.io.vocabmate.fauna.graphql.generated"

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    test {
        useJUnitPlatform()
    }

    // band-aided solution for now
    graphqlGenerateClient {
        doLast {
            val targetPattern = Regex("^import.*generated.((?!enums).).*__UNKNOWN_VALUE$")
            outputs.files.first()
                .walkTopDown()
                .filter { f -> f.isFile && f.readLines().any { line -> line.contains(targetPattern) } }
                .forEach { f ->
                    val builder = f.readLines()
                        .fold(StringBuilder()) { builder, line ->
                            builder
                                .takeUnless { line.contains(targetPattern) }
                                ?.append(line)?.append("\n")
                                ?: builder
                        }
                    val originalPath = f.path
                    f.delete()
                    File(originalPath).apply {
                        writeText(builder.toString())
                        createNewFile()
                    }
                }
        }
    }
}

graphql {
    client {
        packageName = graphqlGeneratedClientPackage
        endpoint = "https://graphql.fauna.com/graphql"
        headers = mapOf("Authorization" to "bearer ${System.getenv("FAUNA_API_KEY")}")
        queryFileDirectory = project.projectDir.resolve("src/main/resources/graphql").path
    }
}
