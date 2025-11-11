# JNI Tutorial - Two Separate Native Libraries

## 🎯 Project Overview

This project demonstrates Java Native Interface (JNI) with **two separate native libraries** built by different build systems:

| Aspect | Gradle Library | CMake Library |
|--------|---|---|
| **Library Name** | libgreetingsGradle.so | libgreetingsCMake.so |
| **Source File** | greetings.cpp | greetings_cmake.cpp |
| **Build System** | Gradle C++ Plugin | CMake |
| **Java Method** | greetingFromGradle() | greetingFromCMake() |
| **Output Directory** | build/libs/greetingsGradle/shared/ | build_cmake/lib/ |
| **Size** | ~24KB | ~17KB |

---

## 📁 Project Structure

```
lib-from-scratch/
├── CMakeLists.txt                          # CMake configuration (libgreetingsCMake.so)
├── README.md                               # Original documentation
├── settings.gradle                         # Gradle settings
├── gradle/
│   └── libs.versions.toml                  # Gradle version catalog
├── app/
│   ├── build.gradle                        # Gradle config (libgreetingsGradle.so)
│   └── src/
│       ├── main/
│       │   ├── java/org/example/App.java   # Java code with 2 native methods
│       │   ├── cpp/
│       │   │   ├── greetings.cpp           # Gradle C++ - greetingFromGradle()
│       │   │   └── greetings_cmake.cpp     # CMake C++ - greetingFromCMake()
│       │   └── resources/
│       └── test/
│           ├── java/org/example/AppTest.java  # 3 JUnit tests
│           └── resources/
├── build/                                  # Gradle output directory
│   ├── libs/greetingsGradle/shared/
│   │   └── libgreetingsGradle.so
│   └── ...
└── build_cmake/                            # CMake output directory
    ├── lib/
    │   └── libgreetingsCMake.so
    └── ...
```

---

## 🔄 Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│              Java Application (App.java)                │
│                                                         │
│  static {                                               │
│    System.loadLibrary("greetingsGradle");  ─────┐       │
│    System.loadLibrary("greetingsCMake");   ─┐   │       │
│  }                                          │   │       │
│                                             │   │       │
│  greetingFromGradle()   ──────────────────┐ │   │       │
│  greetingFromCMake()    ──────────────┐   │ │   │       │
└──────────────────────────────────────┼─┼─┼─┼───┘
                                       │ │ │ │
           ┌───────────────────────────┘ │ │ │
           │   ┌───────────────────────┐ │ │
           │   │   ┌──────────────────┐│ │ │
           ▼   │   │                  ││ │ │
    ┌──────────────────┐       ┌───────────────────┐
    │ libgreetingsGradle.so    │ libgreetingsCMake.so  │
    │  (24KB)                  │  (17KB)           │
    │                          │                   │
    │  Java_...greetingFrom    │  Java_...greetingFrom
    │    Gradle()              │    CMake()        │
    │                          │                   │
    │  [Gradle]Hello from      │  [CMake]Hello from
    │   Gradle C++ Plugin!     │   CMake...!       │
    │                          │                   │
    └──────────────────┘       └───────────────────┘
         Built by:                  Built by:
      Gradle C++ Plugin             CMake 3.10+
         (app/build.gradle)         (CMakeLists.txt)
```

---

## 🚀 Build Flow

### Step 1: Gradle Build Process

```
$ ./gradlew clean build

1. Gradle C++ Plugin compiles greetings.cpp ONLY
   └─ Produces: build/libs/greetingsGradle/shared/libgreetingsGradle.so
   └─ Contains: Java_org_example_App_greetingFromGradle()

2. CMake build task runs as dependency (buildNativeWithCMake)
   └─ Compiles greetings_cmake.cpp ONLY
   └─ Produces: build_cmake/lib/libgreetingsCMake.so
   └─ Contains: Java_org_example_App_greetingFromCMake()

3. Java compilation
   └─ Compiles App.java with native method declarations
   └─ Static initializer loads BOTH libraries

4. Test phase
   └─ java.library.path includes: build/libs/greetingsGradle/shared:build_cmake/lib
   └─ Both libraries loaded before tests execute
```

### Step 2: Runtime Execution

```
$ ./gradlew run

JVM starts with java.library.path pointing to both directories:
  ${buildDir}/libs/greetingsGradle/shared:${rootProject.projectDir}/build_cmake/lib

App.java static initializer:
  1. System.loadLibrary("greetingsGradle");    ← Loads from build/libs/greetingsGradle/shared/
  2. System.loadLibrary("greetingsCMake");     ← Loads from build_cmake/lib/

Both native functions now available to Java code
```

---

## 📦 Getting Started

### Prerequisites

- **Java**: JDK 17.0.16 (configured via gradle toolchain)
- **Gradle**: 9.1.0 (via gradlew)
- **CMake**: 3.10+ (for separate library compilation)
- **GCC**: 11.4.0 (C++ compiler)
- **Linux or macOS** (primary support)

### Installation

```bash
# Clone the repository
git clone https://github.com/ArkadiuszKubiak/JNI_TUTORIAL.git
cd lib-from-scratch

# Ensure gradlew is executable
chmod +x gradlew

# Build both libraries
./gradlew clean build
```

### Commands

| Command | Purpose |
|---------|---------|
| `./gradlew clean build` | Compile all sources (Java + C++ from both systems) |
| `./gradlew run` | Execute application calling both native methods |
| `./gradlew test` | Run all 3 unit tests |
| `./gradlew assemble` | Build without running tests |
| `./gradlew clean` | Remove build artifacts |

---

## 📝 Java Code Explanation

### App.java - Static Initializer (TWO libraries)

```java
public class App {
    /**
     * Static initializer - runs when the App class is loaded into memory.
     * Loads TWO SEPARATE native libraries:
     * 1. libgreetingsGradle.so - Contains greetingFromGradle() function
     * 2. libgreetingsCMake.so - Contains greetingFromCMake() function
     */
    static {
        // Load Gradle-compiled library
        System.loadLibrary("greetingsGradle");
        
        // Load CMake-compiled library
        System.loadLibrary("greetingsCMake");
    }

    /**
     * Native method from libgreetingsGradle.so
     * Compiled by: Gradle C++ Plugin
     * Source: app/src/main/cpp/greetings.cpp
     */
    public native String greetingFromGradle();

    /**
     * Native method from libgreetingsCMake.so
     * Compiled by: CMake
     * Source: app/src/main/cpp/greetings_cmake.cpp
     */
    public native String greetingFromCMake();

    public static void main(String[] args) {
        App app = new App();
        System.out.println(app.greetingFromGradle());
        System.out.println(app.greetingFromCMake());
    }
}
```

---

## 🔧 C++ Code Explanation

### greetings.cpp - Gradle Version

```cpp
/**
 * Compiled by: Gradle C++ Plugin
 * Output: libgreetingsGradle.so
 * Function: Java_org_example_App_greetingFromGradle
 */
extern "C" {
    JNIEXPORT jstring JNICALL Java_org_example_App_greetingFromGradle(
        JNIEnv *env,      // JNI environment interface
        jobject obj       // "this" pointer for instance methods
    )
    {
        // Create C++ string
        std::string greeting = "Hello from Gradle C++ Plugin!";
        
        // Print to console (visible when running)
        std::cout << "[Gradle] " << greeting << std::endl;
        
        // Convert to Java string and return
        return env->NewStringUTF(greeting.c_str());
    }
}
```

### greetings_cmake.cpp - CMake Version

```cpp
/**
 * Compiled by: CMake
 * Output: libgreetingsCMake.so
 * Function: Java_org_example_App_greetingFromCMake
 */
extern "C" {
    JNIEXPORT jstring JNICALL Java_org_example_App_greetingFromCMake(
        JNIEnv *env,      // JNI environment interface
        jobject obj       // "this" pointer for instance methods
    )
    {
        // Create C++ string
        std::string greeting = "Hello from CMake Build System!";
        
        // Print to console (visible when running)
        std::cout << "[CMake] " << greeting << std::endl;
        
        // Convert to Java string and return
        return env->NewStringUTF(greeting.c_str());
    }
}
```

**Key Difference**: Each function has its own library, built independently!

---

## ⚙️ Build Configuration Details

### Gradle Configuration (app/build.gradle)

```groovy
model {
    components {
        // Library name: greetingsGradle (NOT "greetings")
        // This creates libgreetingsGradle.so
        greetingsGradle(NativeLibrarySpec) {
            sources {
                cpp {
                    source {
                        srcDir "src/main/cpp"
                        // Include ONLY greetings.cpp (exclusive to Gradle)
                        include "**/greetings.cpp"
                    }
                }
            }
        }
    }
}

// CMake build task runs BEFORE tests
tasks.named('test') {
    dependsOn buildNativeWithCMake
    
    // java.library.path includes BOTH directories
    systemProperty 'java.library.path', 
        "${buildDir}/libs/greetingsGradle/shared:${rootProject.projectDir}/build_cmake/lib"
}
```

### CMake Configuration (CMakeLists.txt)

```cmake
# Library name: greetingsCMake (NOT "greetings")
# This creates libgreetingsCMake.so
add_library(greetingsCMake SHARED
    app/src/main/cpp/greetings_cmake.cpp  # ONLY this file
)

# Output location
set_target_properties(greetingsCMake PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
)
```

---

## ✅ Testing

### Run Tests

```bash
./gradlew test
```

### Test Methods (3 total)

| Test | Purpose | Library |
|------|---------|---------|
| `appHasAGreeting()` | Tests Java method | None (Java only) |
| `appHasGradleNativeGreeting()` | Tests greetingFromGradle() | libgreetingsGradle.so |
| `appHasCMakeNativeGreeting()` | Tests greetingFromCMake() | libgreetingsCMake.so |

### Expected Output

```
BUILD SUCCESSFUL

> Task :app:test
> Task :app:buildNativeWithCMake
[CMake] Hello from CMake Build System!
[Gradle] Hello from Gradle C++ Plugin!

Tests completed: 3 passed
```

---

## 🎯 Architecture Comparison

### Before (Single Library)
```
Java App
  ├─ greetingFromGradle() → libgreetings.so (contained both functions)
  └─ greetingFromCMake()
```

### Now (Two Separate Libraries) ✨
```
Java App
  ├─ System.loadLibrary("greetingsGradle") → libgreetingsGradle.so
  │  └─ greetingFromGradle()
  └─ System.loadLibrary("greetingsCMake") → libgreetingsCMake.so
     └─ greetingFromCMake()
```

**Benefits:**
- ✅ Clear separation of concerns
- ✅ Independent compilation paths
- ✅ Easier to understand and modify
- ✅ Can swap build systems without affecting other code
- ✅ Demonstrates advanced JNI multi-library loading

---

## 🔍 JNI Concepts Used

### 1. **Native Library Loading**
```java
System.loadLibrary("libraryName");  // Searches java.library.path
System.load("/full/path/to/library.so");  // Explicit path
```

### 2. **JNI Naming Convention**
```
Java method:   greetingFromGradle()
JNI function:  Java_org_example_App_greetingFromGradle
               Java_[package]_[class]_[method]
               (dots replaced with underscores)
```

### 3. **Type Mappings**
| Java | C++ | Description |
|------|-----|-------------|
| String | jstring | Java string object |
| void | void | No return value |
| int | jint | 32-bit integer |
| long | jlong | 64-bit integer |

### 4. **JNIEnv Functions**
```cpp
env->NewStringUTF(const char *bytes)    // Create Java String from C string
env->GetStringUTFChars(jstring str)     // Get C string from Java String
env->CallIntMethod(jobject obj, ...)    // Call Java method from C++
```

---

## 📊 File Sizes After Compilation

```
build/libs/greetingsGradle/shared/libgreetingsGradle.so  [24 KB]
build_cmake/lib/libgreetingsCMake.so                      [17 KB]
Total native code: 41 KB
```

---

## 🔗 Application Output

```bash
$ ./gradlew run

> Task :app:run
Hello World!                           (Java method - getGreeting())
[Gradle] Hello from Gradle C++ Plugin!  (Gradle native + console output)
Hello from Gradle C++ Plugin!           (Gradle native return value)
[CMake] Hello from CMake Build System!  (CMake native + console output)
Hello from CMake Build System!          (CMake native return value)

BUILD SUCCESSFUL in 587ms
```

---

## 🚨 Troubleshooting

### Error: `UnsatisfiedLinkError: no greetingsCMake in java.library.path`
**Solution**: Ensure CMake build runs before tests:
```groovy
tasks.named('test') {
    dependsOn buildNativeWithCMake
    systemProperty 'java.library.path', "${buildDir}/libs/greetingsGradle/shared:${rootProject.projectDir}/build_cmake/lib"
}
```

### Error: `UnsatisfiedLinkError: no greetingsGradle in java.library.path`
**Solution**: Check Gradle library name in build.gradle:
```groovy
greetingsGradle(NativeLibrarySpec) {  // Must be "greetingsGradle"
```

### Error: CMake not found
**Solution**: Install CMake:
```bash
sudo apt-get install cmake   # Ubuntu/Debian
brew install cmake           # macOS
```

### Error: JNI headers not found
**Solution**: Gradle automatically handles JNI includes. For CMake, ensure:
```bash
find /usr/lib/jvm/default-java/include -name "jni.h"
```

---

## 📚 Learning Resources

### JNI Documentation
- [Oracle JNI Documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/jni/index.html)
- [JNI Type Mappings](https://docs.oracle.com/javase/8/docs/technotes/jni/spec/types.html)
- [JNI Functions Reference](https://docs.oracle.com/javase/8/docs/technotes/jni/spec/functions.html)

### CMake Documentation
- [CMake Official](https://cmake.org/)
- [Finding Java with CMake](https://cmake.org/cmake/help/latest/module/FindJava.html)
- [Finding JNI with CMake](https://cmake.org/cmake/help/latest/module/FindJNI.html)

### Gradle C++ Plugin
- [Gradle C++ Plugin Documentation](https://docs.gradle.org/current/userguide/cpp_plugin.html)
- [Native Build Model](https://docs.gradle.org/current/userguide/native_binaries.html)

---

## 📋 Summary

This project demonstrates:

1. ✅ **Two independent native libraries** built by different systems
2. ✅ **Loading multiple JNI libraries** from Java
3. ✅ **Gradle C++ Plugin** for native compilation
4. ✅ **CMake integration** with Gradle
5. ✅ **JNI naming conventions** and function signatures
6. ✅ **Type conversion** between Java and C++
7. ✅ **Unit testing** with JUnit 5
8. ✅ **Professional documentation** and code comments
9. ✅ **Version control** with Git
10. ✅ **Cross-build-system coordination**

---

## 👤 Author & Stack

**Author**: Arkadiusz Kubiak (a.j.kubiak93@gmail.com)

**Technology Stack**:
- **Language**: Java 25, C++11
- **JDK**: 17.0.16
- **Gradle**: 9.1.0
- **CMake**: 3.10+
- **Compiler**: GCC 11.4.0
- **Test Framework**: JUnit 5 (Jupiter)
- **Version Control**: Git
- **Repository**: https://github.com/ArkadiuszKubiak/JNI_TUTORIAL

---

## 🔗 Quick Links

- **GitHub Repository**: https://github.com/ArkadiuszKubiak/JNI_TUTORIAL
- **Current Branch**: master
- **Java Source**: app/src/main/java/org/example/App.java
- **C++ Gradle**: app/src/main/cpp/greetings.cpp
- **C++ CMake**: app/src/main/cpp/greetings_cmake.cpp
- **Gradle Config**: app/build.gradle
- **CMake Config**: CMakeLists.txt
- **Tests**: app/src/test/java/org/example/AppTest.java

