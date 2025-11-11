# JNI Course Project: Java Native Interface with Dual Build Systems

A comprehensive Java Native Interface (JNI) project demonstrating how to integrate native C++ code with Java applications using two different build systems: **Gradle C++ Plugin** and **CMake**. This project is designed as an educational resource for learning JNI concepts and native integration patterns.

---

## 📋 Project Overview

This project provides a complete example of calling native C++ functions from Java code, with the ability to build using either:
- **Gradle C++ Plugin** (default, built-in to Gradle)
- **CMake** (alternative, independent build system)

Both build systems compile the same JNI functionality into a shared library (`libgreetings.so`) that can be loaded and called from Java.

---

## 📁 Project Structure

```
lib-from-scratch/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/
│   │   │   │   └── App.java                    # Main Java application with native methods
│   │   │   └── cpp/
│   │   │       ├── greetings.cpp               # C++ source for Gradle C++ Plugin build
│   │   │       └── greetings_cmake.cpp         # C++ source for CMake build
│   │   └── test/
│   │       └── java/org/example/
│   │           └── AppTest.java                # JUnit 5 unit tests
│   ├── build.gradle                            # Primary build configuration (Gradle C++ Plugin)
│   └── build.gradle.plugin                     # Backup Gradle C++ Plugin configuration
│
├── gradle/
│   ├── libs.versions.toml                      # Dependency version management
│   └── wrapper/                                # Gradle wrapper files
│
├── CMakeLists.txt                              # CMake build configuration
├── build.gradle.cmake                          # Alternative CMake-based Gradle configuration
├── settings.gradle                             # Gradle settings
├── gradle.properties                           # Gradle global properties
├── gradlew & gradlew.bat                       # Gradle wrapper executables
└── README.md                                   # This file
```

---

## 🏗️ Build Architecture

### Overview

The project uses Gradle as the primary orchestrator with two backend options for native compilation:

```
Gradle Build System
    ├─── Option 1: Gradle C++ Plugin (PRIMARY)
    │    └── Compiles greetings.cpp → libgreetings.so
    │        Location: ${buildDir}/libs/greetings/shared/
    │
    └─── Option 2: CMake Backend (ALTERNATIVE)
         └── Compiles greetings_cmake.cpp → libgreetings.so
             Location: build_cmake/lib/
```

Both produce the same library interface but allow different compilation strategies and tooling preferences.

---

## 🔧 Part 1: Building with Gradle C++ Plugin (DEFAULT)

### What is the Gradle C++ Plugin?

The Gradle C++ Plugin is a built-in Gradle feature that enables native C++ compilation directly through Gradle tasks. It provides:
- **Seamless Java integration** - Native libraries built alongside Java code
- **Automatic dependency management** - Gradle handles all compilation steps
- **Built-in support** - No additional software installation needed beyond Java and C++ compiler
- **Configuration in build.gradle** - Single file controls both Java and C++ builds

### Configuration Files

**Primary:** `app/build.gradle`

Key configuration sections:
```gradle
plugins {
    id 'application'  // Java application support
    id 'cpp'          // C++ native compilation
}

model {
    components {
        greetings(NativeLibrarySpec) {
            sources {
                cpp {
                    source {
                        srcDir "src/main/cpp"
                        include "**/greetings.cpp"  // Include only greetings.cpp
                    }
                }
            }
        }
    }
}
```

### Building with Gradle C++ Plugin

```bash
# Clean build with tests
./gradlew clean build

# Build only (skip tests)
./gradlew build -x test

# Run the application
./gradlew run

# Run unit tests
./gradlew test

# Build with detailed output
./gradlew build --info
```

### Output Location

- **Compiled library:** `app/build/libs/greetings/shared/libgreetings.so`
- **Java classes:** `app/build/classes/java/main/`
- **Test results:** `app/build/test-results/`

### Native Code

**File:** `app/src/main/cpp/greetings.cpp`

This C++ file contains the JNI function that implements the native "greetings" method:

```cpp
JNIEXPORT jstring JNICALL Java_org_example_App_greetings(JNIEnv *env, jobject obj)
```

**Function name explanation:**
- `Java_` - JNI prefix (required)
- `org_example_App` - Package and class name (dots replaced with underscores)
- `greetings` - Method name from Java

**Key features:**
- Uses C++ STL (std::string) for string handling
- Converts C++ std::string to Java jstring using JNI's NewStringUTF
- Prints debug information to stdout
- Returns a greeting message to Java

### Compiler Flags (Gradle C++ Plugin)

**Linux settings:**
```gradle
cppCompiler.args "-fPIC",                                    // Position-independent code
                 "-I/usr/lib/jvm/default-java/include",     // JNI headers
                 "-I/usr/lib/jvm/default-java/include/linux" // Linux-specific JNI headers
```

**Linker flags:**
```gradle
linker.args "-L/usr/lib/jvm/default-java/lib/server", "-ljvm"  // Link with JVM library
```

### Advantages of Gradle C++ Plugin

✅ **Built-in** - No additional tools needed beyond Gradle  
✅ **Fast builds** - Optimized for development  
✅ **Excellent Java integration** - Native libraries automatically available to tests and runtime  
✅ **Single configuration file** - All settings in `app/build.gradle`  
✅ **Incremental compilation** - Only recompiles changed files  

### Test Execution (Gradle C++ Plugin)

Tests are executed with proper library paths configured:

```gradle
tasks.named('test') {
    useJUnitPlatform()  // Use JUnit 5
    systemProperty 'java.library.path', "${buildDir}/libs/greetings/shared"  // Library location
    jvmArgs '--enable-native-access=ALL-UNNAMED'  // JNI support (Java 19+)
}
```

---

## 🔧 Part 2: Building with CMake (ALTERNATIVE)

### What is CMake?

CMake is an independent, industry-standard build system that generates platform-specific build files (Makefiles, Visual Studio projects, etc.). It provides:
- **Complete independence** - Can be used without Gradle for C++ projects
- **Platform flexibility** - Works on Linux, macOS, Windows with native tools
- **Better debugging** - More detailed control over compilation process
- **Reusable** - CMake scripts can be used in other projects
- **Industry standard** - Widely used in C++ ecosystem

### Configuration Files

**Primary:** `CMakeLists.txt` - CMake build instructions  
**Gradle wrapper:** `build.gradle.cmake` - Makes CMake available through Gradle

#### CMakeLists.txt Structure

```cmake
# FIND REQUIRED PACKAGES
find_package(Java REQUIRED)
find_package(JNI REQUIRED)

# CREATE SHARED LIBRARY
add_library(greetings SHARED app/src/main/cpp/greetings_cmake.cpp)

# CONFIGURE INCLUDE PATHS
target_include_directories(greetings PRIVATE ${JNI_INCLUDE_DIRS})

# LINK LIBRARIES
target_link_libraries(greetings ${JNI_LIBRARIES})

# SET OUTPUT DIRECTORY
set(LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib")

# COMPILATION FLAGS
target_compile_options(greetings PRIVATE -fPIC -Wall -Wextra -O2)
set_target_properties(greetings PROPERTIES POSITION_INDEPENDENT_CODE ON)
```

**Key CMake sections:**
1. **Find packages** - Locates Java Development Kit and JNI libraries
2. **Create library** - Defines shared library target and source files
3. **Include paths** - Adds JNI header directories
4. **Link libraries** - Links against JVM library
5. **Output directory** - Sets where compiled library is placed
6. **Compilation flags** - Specifies compiler optimization and warning levels

#### build.gradle.cmake Structure

This file integrates CMake with Gradle:

```gradle
// Executes CMake via bash command
task buildNativeWithCMake(type: Exec) {
    commandLine "bash", "-c", """
        mkdir -p build_cmake
        cd build_cmake
        cmake -DCMAKE_BUILD_TYPE=Release ..
        cmake --build .
    """
}

// Sets java.library.path to CMake output directory
tasks.withType(JavaExec) {
    def libPath = new File(rootProject.projectDir, 'build_cmake/lib').absolutePath
    systemProperty 'java.library.path', libPath
}
```

### Switching to CMake Build System

Step 1: Backup current (Gradle plugin) build configuration
```bash
cp app/build.gradle app/build.gradle.plugin
```

Step 2: Activate CMake build configuration
```bash
cp build.gradle.cmake app/build.gradle
```

Step 3: Build the project
```bash
./gradlew clean build
```

### Building with CMake

```bash
# Clean build with tests (via Gradle)
./gradlew clean build

# Build only (skip tests)
./gradlew build -x test

# Run the application
./gradlew run

# Run unit tests
./gradlew test

# Manual CMake compilation (without Gradle)
mkdir -p build_cmake
cd build_cmake
cmake -DCMAKE_BUILD_TYPE=Release ..
cmake --build .
```

### Output Location

- **Compiled library:** `build_cmake/lib/libgreetings.so`
- **CMake build directory:** `build_cmake/` (completely separate from Gradle build/)
- **Build artifacts:** `build_cmake/CMakeFiles/`, `build_cmake/CMakeCache.txt`

### Native Code

**File:** `app/src/main/cpp/greetings_cmake.cpp`

Identical to `greetings.cpp` but produces output prefixed with `[CMake]` to distinguish it from the Gradle plugin version:

```cpp
JNIEXPORT jstring JNICALL Java_org_example_App_greetings(JNIEnv *env, jobject obj) {
    // Same JNI implementation
    // Output: "[CMake] Greetings from CMake C++ code!"
}
```

### Compiler Flags (CMake)

**Debug & optimization:**
```cmake
-DCMAKE_BUILD_TYPE=Release  # Release build with optimizations
```

**Compiler warnings and code generation:**
```cmake
-fPIC                       # Position-independent code
-Wall -Wextra               # All warnings enabled
-O2                         # Medium optimization level
```

### Advantages of CMake

✅ **Independent** - Works without Gradle in any C++ project  
✅ **Standard tool** - Used across industry for C++ projects  
✅ **Better debugging** - More control over compilation process  
✅ **Platform portable** - Same CMakeLists.txt on Linux, macOS, Windows  
✅ **Better error messages** - More detailed compilation diagnostics  
✅ **Reusable** - CMake scripts can be used in other projects  

### Test Execution (CMake)

Tests are executed with library path pointing to CMake output:

```gradle
tasks.named('test') {
    useJUnitPlatform()  // Use JUnit 5
    def libPath = new File(rootProject.projectDir, 'build_cmake/lib').absolutePath
    systemProperty 'java.library.path', libPath  // CMake library location
    jvmArgs '--enable-native-access=ALL-UNNAMED'  // JNI support (Java 19+)
}
```

---

## 🔄 Switching Between Build Systems

### From Gradle C++ Plugin to CMake

```bash
# Backup current Gradle plugin configuration
cp app/build.gradle app/build.gradle.plugin

# Activate CMake configuration
cp build.gradle.cmake app/build.gradle

# Clean rebuild with CMake
./gradlew clean build
```

### From CMake Back to Gradle C++ Plugin

```bash
# Restore Gradle plugin configuration
cp app/build.gradle.plugin app/build.gradle

# Clean rebuild with Gradle plugin
./gradlew clean build
```

### Verifying Active Build System

Check which configuration is active:
```bash
# See current build.gradle content
head -5 app/build.gradle

# Gradle C++ Plugin shows:
# plugins { id 'cpp' ... }

# CMake version shows:
# task buildNativeWithCMake(type: Exec) { ... }
```

---

## 📊 Comparison: Gradle C++ Plugin vs CMake

| Aspect | Gradle C++ Plugin | CMake |
|--------|------------------|-------|
| **Setup Complexity** | Simple (built-in) | Moderate (needs installation) |
| **Build Speed** | Fast | Normal |
| **Java Integration** | Excellent | Good |
| **IDE Integration** | Excellent | Good |
| **Debugging** | OK | Excellent |
| **Portability** | Gradle-dependent | Completely independent |
| **Learning Curve** | Easy | Moderate |
| **Use Outside Gradle** | ❌ No | ✅ Yes |
| **Configuration File** | `app/build.gradle` | `CMakeLists.txt` |
| **Native Source** | `greetings.cpp` | `greetings_cmake.cpp` |
| **Output Location** | `build/libs/greetings/shared/` | `build_cmake/lib/` |

**Choose Gradle C++ Plugin if:**
- You want simplicity and quick setup
- You're primarily working in Java ecosystem
- You don't need CMake for other projects
- Fast development builds are priority

**Choose CMake if:**
- You want complete independence from Gradle
- You use CMake in other projects
- You need better debugging capabilities
- You want industry-standard build system
- You target multiple platforms

---

## 🛠️ Java Application Structure

### Main Application Class

**File:** `app/src/main/java/org/example/App.java`

```java
public class App {
    static {
        // Static initializer - runs when class is loaded
        // Loads the native library into the JVM
        System.loadLibrary("greetings");
    }
    
    // Native method declaration
    // Implementation is in C++ (greetings.cpp or greetings_cmake.cpp)
    public native String greetings();
    
    // Pure Java method for comparison
    public static String getGreeting() {
        return "Hello World!";
    }
    
    public static void main(String[] args) {
        App app = new App();
        System.out.println(app.getGreeting());    // Java method
        System.out.println(app.greetings());      // Native method
    }
}
```

**Key JNI Concepts:**

1. **Static initializer** - Loads `libgreetings.so` using `System.loadLibrary()`
2. **Native method declaration** - No implementation body, just signature
3. **JNI naming convention** - C++ function must match `Java_package_class_method` pattern

### Test Class

**File:** `app/src/test/java/org/example/AppTest.java`

```java
class AppTest {
    @Test
    void appHasAGreeting() {
        // Tests that pure Java method returns non-null value
        assertNotNull(App.getGreeting());
    }
    
    @Test
    void appHasNativeGreeting() {
        // Tests that native method is available and returns a greeting
        App app = new App();
        String greeting = app.greetings();
        assertNotNull(greeting);
        assertTrue(greeting.contains("C++"));  // Verify native code ran
    }
}
```

**Test Verification:**
- Tests that both Java and native methods work correctly
- Verifies native library was successfully loaded
- Confirms JNI function was properly called

---

## 🚀 Common Tasks

### Building the Project

```bash
# Full clean build (includes tests)
./gradlew clean build

# Build without running tests
./gradlew build -x test

# Build with verbose output
./gradlew build --info
```

### Running the Application

```bash
# Run with Gradle
./gradlew run

# Run manually (after build)
java -Djava.library.path=app/build/libs/greetings/shared -cp app/build/classes/java/main org.example.App
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests AppTest

# Run with detailed output
./gradlew test --info
```

### Cleaning

```bash
# Clean Gradle build artifacts
./gradlew clean

# Clean CMake build artifacts (only when using CMake)
rm -rf build_cmake/

# Full clean (both systems)
./gradlew clean && rm -rf build_cmake/
```

---

## 📦 Dependencies

### Java Dependencies

**Defined in:** `gradle/libs.versions.toml`

```
[versions]
junit = "5.x.x"

[libraries]
junit-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit" }
```

### System Dependencies

**Required:**
- **Java Development Kit (JDK) 25** - For Java compilation and JNI headers
- **C++ Compiler** - GCC (Linux), Clang (macOS), MSVC (Windows)

**Optional (for CMake system):**
- **CMake 3.10+** - For CMake-based compilation

### Installation

**Linux (Ubuntu/Debian):**
```bash
# Install Java development kit
sudo apt-get install default-jdk

# Install C++ compiler
sudo apt-get install build-essential

# Install CMake (if using CMake system)
sudo apt-get install cmake
```

**macOS:**
```bash
# Install Xcode Command Line Tools
xcode-select --install

# Install CMake (if using CMake system)
brew install cmake
```

---

## 🔗 JNI Function Naming Convention

All native functions in JNI must follow a specific naming pattern:

```
Java_<package>_<class>_<methodName>
```

**Example Breakdown:**

For method `greetings()` in class `org.example.App`:

```
Java_org_example_App_greetings
 │    │   │      │   └─ Method name
 │    │   │      └────── Class name
 │    │   └───────────── Package name (dots → underscores)
 │    └────────────────── Package/class separator
 └──────────────────────── JNI prefix (required)
```

**JNI Function Signature:**
```c
JNIEXPORT jstring JNICALL Java_org_example_App_greetings(
    JNIEnv *env,      // Pointer to JNI environment
    jobject obj       // Reference to 'this' object
)
```

---

## 🎯 Expected Output

### Gradle C++ Plugin Build Output

```bash
$ ./gradlew run

> Task :app:run
Hello World!
Greetings from native C++ code!
```

### CMake Build Output

```bash
$ ./gradlew run

> Task :app:run
Hello World!
[CMake] Greetings from CMake C++ code!
```

### Test Output

```bash
$ ./gradlew test

> Task :app:test
BUILD SUCCESSFUL in Xs

Tests:
- appHasAGreeting PASSED
- appHasNativeGreeting PASSED
```

---

## 🔧 Environment Configuration

### Java Home

Ensure JAVA_HOME points to JDK:

```bash
# Check current JAVA_HOME
echo $JAVA_HOME

# Set JAVA_HOME (Linux/macOS)
export JAVA_HOME=/usr/lib/jvm/default-java

# Set JAVA_HOME (macOS with Homebrew)
export JAVA_HOME=$(/usr/libexec/java_home)

# Make permanent (add to ~/.bashrc or ~/.zshrc)
echo 'export JAVA_HOME=/usr/lib/jvm/default-java' >> ~/.bashrc
```

### CMake Path

Verify CMake is available (only if using CMake system):

```bash
# Check CMake availability
which cmake
cmake --version

# Install if missing
# Linux: sudo apt-get install cmake
# macOS: brew install cmake
```

---

## 🐛 Troubleshooting

### Error: "Cannot find libgreetings.so"

**Cause:** Native library was not built or java.library.path is incorrect

**Solution:**
```bash
# Rebuild the project
./gradlew clean build

# Verify library exists
# For Gradle: ls app/build/libs/greetings/shared/libgreetings.so
# For CMake: ls build_cmake/lib/libgreetings.so
```

### Error: "Undefined reference to JNI functions"

**Cause:** JNI headers or JVM library not linked

**Solution:**
```bash
# Verify JAVA_HOME is set correctly
echo $JAVA_HOME

# Check JNI files exist
ls $JAVA_HOME/include/
ls $JAVA_HOME/include/linux/
ls $JAVA_HOME/lib/server/libjvm.so
```

### Error: "CMake not found" (when using CMake)

**Cause:** CMake is not installed or not in PATH

**Solution:**
```bash
# Install CMake
# Linux: sudo apt-get install cmake
# macOS: brew install cmake

# Verify installation
cmake --version
```

### Error: "java.library.path not configured"

**Cause:** Tests running with wrong library path

**Solution:**
- Gradle plugin: Library should be in `app/build/libs/greetings/shared/`
- CMake: Library should be in `build_cmake/lib/`
- Verify correct `build.gradle` is active

### Build fails with "Java version mismatch"

**Cause:** Build requires Java 25 but system has different version

**Solution:**
```bash
# Check installed Java versions
java -version
javac -version

# Install Java 25
# Download from oracle.com or use package manager

# Set correct JAVA_HOME
export JAVA_HOME=/path/to/java/25
```

---

## 📚 Learning Resources

### JNI Documentation
- [Java Native Interface (JNI) Specification](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/)
- [JNI Programmer's Guide](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/design.html)
- [Type Mappings in JNI](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/types.html)

### Build Systems
- [Gradle C++ Plugin Documentation](https://docs.gradle.org/current/userguide/cpp_plugin.html)
- [CMake Tutorial](https://cmake.org/cmake/help/latest/guide/tutorial/)
- [CMake Documentation](https://cmake.org/cmake/help/latest/)

### C++ and Java Integration
- [C++ and JVM Interoperability](https://docs.oracle.com/javase/tutorial/jni/)
- [GCC C++ Compiler Manual](https://gcc.gnu.org/onlinedocs/gcc/)
- [Understanding Shared Libraries](https://tldp.org/HOWTO/Program-Library-HOWTO/)

---

## 📝 Project Details

**Purpose:** Educational JNI project for learning Java Native Interface concepts

**Technologies:**
- **Java 25** - Primary programming language
- **C++ 11+** - Native code implementation
- **Gradle 9.1.0** - Build orchestration and Java compilation
- **CMake 3.10+** - Alternative native compilation
- **JUnit 5** - Unit testing framework
- **Linux/GCC** - Build environment

**Project Type:** Dual build system demonstration

**Learning Objectives:**
1. Understand JNI function naming and signatures
2. Learn Gradle C++ plugin integration with Java
3. Learn CMake build system fundamentals
4. Practice switching between different build systems
5. Experience native code debugging and testing

---

## 📄 License and Attribution

**Course Project:** JNI Course - Learn Java Native Interface

This project is designed for educational purposes to demonstrate JNI concepts and best practices in native code integration.
