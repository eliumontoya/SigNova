plugins {
    id("com.android.application")
}

android {
    namespace = "info.eliumontoyasadec.signova"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "info.eliumontoyasadec.signova"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 🔐 API KEY desde gradle.properties o ~/.gradle/gradle.properties
        val openAiKey = providers.gradleProperty("OPENAI_API_KEY").orNull
            ?: System.getenv("OPENAI_API_KEY")
            ?: ""

        if (openAiKey.isBlank()) {
            throw GradleException(
                "OPENAI_API_KEY no está definida. " +
                        "Agrégala en gradle.properties o ~/.gradle/gradle.properties"
            )
        }

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"$openAiKey\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

/**
 * ⚠️ Mantén UNA sola versión de RecyclerView
 * (1.3.2 es compatible con compileSdk 34)
 */
configurations.all {
    resolutionStrategy {
        force("androidx.recyclerview:recyclerview:1.3.2")
    }
}

dependencies {
    implementation ("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")

    implementation("androidx.navigation:navigation-fragment:2.9.5")
    implementation("androidx.navigation:navigation-ui:2.9.5")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}