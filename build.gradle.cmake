// ==================== GRADLE PLUGINS ====================
// Apply plugin to support Java application builds
// Note: This configuration uses CMake for native compilation (no 'cpp' plugin needed)
plugins {
    id 'application'  // Plugin for building runnable Java applications
}

// ==================== DEPENDENCY REPOSITORIES ====================
// Configure where to download project dependencies from
repositories {
    // Use Maven Central Repository for Java libraries
    mavenCentral()
}

// ==================== JAVA DEPENDENCIES ====================
// Define test and runtime dependencies for unit testing
dependencies {
    // JUnit 5 (Jupiter) for unit testing
    testImplementation libs.junit.jupiter
    
    // JUnit Platform launcher for running tests
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

// ==================== JAVA TOOLCHAIN CONFIGURATION ====================
// Configure which Java version to use for compilation
java {
    toolchain {
        // Use Java 25 for compilation and runtime
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// ==================== APPLICATION CONFIGURATION ====================
// Configure the main application entry point
application {
    // Specify the main class to run when ./gradlew run is executed
    mainClass = 'org.example.App'
}

// ==================== TEST CONFIGURATION ====================
// Configure how unit tests are executed with CMake-built native library
tasks.named('test') {
    // Use JUnit Platform for running tests (required for JUnit 5)
    useJUnitPlatform()
    
    // Set java.library.path to find native libraries compiled by CMake
    // This path points to build_cmake/lib where CMake outputs libgreetings.so
    def libPath = new File(rootProject.projectDir, 'build_cmake/lib').absolutePath
    systemProperty 'java.library.path', libPath
    
    // Enable native access for JNI (required in Java 19+)
    jvmArgs '--enable-native-access=ALL-UNNAMED'
}

// ==================== CMAKE NATIVE BUILD TASK ====================
// Define a custom Gradle task that invokes CMake to compile native C++ code
// This is an alternative to Gradle's built-in C++ plugin
task buildNativeWithCMake(type: Exec) {
    // Description shown when running ./gradlew tasks
    description = 'Build native libraries using CMake'
    
    // Set working directory to project root (where CMakeLists.txt is located)
    workingDir = rootProject.projectDir
    
    // Execute bash commands to:
    // 1. Create build_cmake directory (or use existing)
    // 2. Run cmake to configure with Release optimization
    // 3. Run cmake --build to compile the native library
    commandLine "bash", "-c", """
        mkdir -p build_cmake
        cd build_cmake
        cmake -DCMAKE_BUILD_TYPE=Release ..
        cmake --build .
    """
}

// ==================== BUILD DEPENDENCY CONFIGURATION ====================
// Make CMake native build a dependency of the main build process
// This ensures native libraries are built before Java compilation
build.dependsOn buildNativeWithCMake

// ==================== JAVA EXECUTION CONFIGURATION ====================
// Configure how Java code is executed (for ./gradlew run)
// Ensures java.library.path points to CMake output directory
tasks.withType(JavaExec) {
    // Calculate absolute path to CMake output library directory
    def libPath = new File(rootProject.projectDir, 'build_cmake/lib').absolutePath
    
    // Set java.library.path to find native libraries at runtime
    systemProperty 'java.library.path', libPath
    
    // Enable native access for JNI (required in Java 19+)
    jvmArgs '--enable-native-access=ALL-UNNAMED'
}

// ==================== CMAKE CLEAN TASK ====================
// Define a custom task to clean CMake build artifacts
// This removes the entire build_cmake directory (different from Gradle's build/)
task cleanCMake(type: Delete) {
    // Delete the build_cmake directory and all its contents
    delete 'build_cmake'
}

// ==================== CLEAN DEPENDENCY CONFIGURATION ====================
// Make cleanCMake a dependency of the main clean task
// This ensures CMake artifacts are removed when running ./gradlew clean
clean.dependsOn cleanCMake
