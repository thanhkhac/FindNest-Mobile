plugins {
    alias(libs.plugins.android.application)
}
android {
    namespace = "com.example.findnest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.findnest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    //for calling api
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    //for logging reqtest body + response
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //for ui
    implementation ("com.google.android.material:material:1.10.0")

    //valid
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")


    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.github.MKergall:osmbonuspack:6.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lombok)
    implementation(libs.osmdroid)
    implementation(libs.play.services.location)
    implementation(libs.imageslideshow)
    implementation(libs.glide)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.glide.compiler)
}