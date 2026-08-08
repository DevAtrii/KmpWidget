plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.bundles.gradle.plugins)
    compileOnly(libs.kotlin.gradle.plugin.api)
    compileOnly(libs.vanniktech.maven.publish.impl)
}

gradlePlugin {
    plugins {
        registerPlugin(
            id = "com.warp.plugins.publish",
            implementationClass = "KmpLibraryPublishPlugin",
        )
    }
}

fun NamedDomainObjectContainer<PluginDeclaration>.registerPlugin(
    id: String,
    implementationClass: String,
) {
    register(id) {
        this.id = id
        this.implementationClass = implementationClass
    }
}
