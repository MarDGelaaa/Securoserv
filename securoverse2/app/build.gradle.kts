plugins {
    id("com.android.application")

    id("com.google.gms.google-services")

}

android {
    namespace = "com.esaip.securoverse2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.esaip.securoverse2"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        exclude("META-INF/androidx.cardview_cardview.version")
    }


}

dependencies {



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("androidx.browser:browser:1.3.0")

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation ("com.google.firebase:firebase-database:20.1.0")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("org.json:json:20210307")

    implementation ("androidx.preference:preference:1.2.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1") // Pour les versions avant Room 2.6.0
    // Pour utiliser Kotlin (si vous utilisez Kotlin)
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-rxjava2:2.6.1")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:2.6.1")

    // Pour les tests
    testImplementation ("androidx.room:room-testing:2.6.1")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:2.6.1")


    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")

    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation ("com.google.android.material:material:1.5.0")



    ///
    // implementation ("androidx.room:room-runtime:2.5.0")
    //annotationProcessor ("androidx.room:room-compiler:2.5.0")
    // Security

    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    implementation ("net.zetetic:android-database-sqlcipher:4.5.4")

    // WorkManager
    implementation ("androidx.work:work-runtime:2.8.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.5.0")



}