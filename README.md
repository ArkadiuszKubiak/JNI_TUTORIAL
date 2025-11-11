# JNI Tutorial: Dual Build Systems with Native C++ Integration

A comprehensive Java Native Interface (JNI) educational project demonstrating how to compile and call native C++ code from Java using **two active build systems simultaneously**: Gradle C++ Plugin and CMake.

Both build systems compile their own C++ implementations into a **single shared library** (`libgreetings.so`) containing two distinct native functions callable from Java.

---

## 📋 Quick Overview

| Aspect | Detail |
|--------|--------|
| **Build System 1** | Gradle C++ Plugin (default) |
| **Build System 2** | CMake (active alternative) |
| **Native Language** | C++ 11+ |
| **JVM** | Java 25 with Java 17.0.16 runtime |
| **Testing** | JUnit 5 (Jupiter) |
| **Output Library** | Single `libgreetings.so` with 2 functions |
| **Platform** | Linux (GCC compiler) |

---

## 📁 Project Structure

```
lib-from-scratch/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/
│   │   │   │   └── App.java                 # Main Java app with 2 native methods
│   │   │   └── cpp/
│   │   │       ├── greetings.cpp            # Gradle version: greetingFromGradle()
│   │   │       └── greetings_cmake.cpp      # CMake version: greetingFromCMake()
│   │   └── test/
│   │       └── java/org/example/
│   │           └── AppTest.java             # JUnit 5 tests for both methods
│   │
│   ├── build.gradle                         # PRIMARY - Both systems active here
│   └── build.gradle.plugin                  # BACKUP - Gradle only
│
├── gradle/
│   ├── libs.versions.toml                   # Dependency versions
│   └── wrapper/                             # Gradle wrapper
│
├── CMakeLists.txt                           # CMake configuration
├── build.gradle.cmake                       # ALTERNATIVE - CMake only
├── settings.gradle
├── gradle.properties
├── gradlew & gradlew.bat
└── README.md
```

---

## 🔄 How It Works

### Architecture Overview

```
┌──────────────────────────────────────────────────────────┐
│         Single app/build.gradle (PRIMARY)                │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ✓ Gradle C++ Plugin (built-in to Gradle)              │
│    └─ Compiles: greetings.cpp                          │
│       Function: greetingFromGradle()                   │
│       Output: "[Gradle] Hello from Gradle..."          │
│                                                          │
│  ✓ CMake Build System (via task)                       │
│    └─ Compiles: greetings.cpp + greetings_cmake.cpp   │
│       Function: greetingFromCMake()                    │
│       Output: "[CMake] Hello from CMake..."            │
│                                                          │
│  ➜ BOTH compile to: libgreetings.so (single file)     │
│  ➜ BOTH functions in same library                      │
│  ➜ Java can call BOTH from same process               │
└──────────────────────────────────────────────────────────┘
```

### Build Flow

```
./gradlew clean build

1. CMake Task (buildNativeWithCMake)
   ├─ Create build_cmake directory
   ├─ Run cmake -DCMAKE_BUILD_TYPE=Release
   └─ Compile: greetings.cpp + greetings_cmake.cpp
      → Output: build_cmake/lib/libgreetings.so

2. Gradle C++ Plugin (model block)
   ├─ Compile: greetings.cpp + greetings_cmake.cpp
   └─ → Output: app/build/libs/greetings/shared/libgreetings.so

3. Java Compilation
   ├─ Compile: App.java
   ├─ Test: AppTest.java
   └─ Load both native libraries via java.library.path

RESULT: Single libgreetings.so with:
  • greetingFromGradle()
  • greetingFromCMake()
```

---

## 🚀 Getting Started

### Prerequisites

**Required:**
- Java Development Kit (JDK) 25
- C++ Compiler (GCC on Linux)

**Optional (already included):**
- CMake 3.10+
- Gradle 9.1.0 (via wrapper)

### Installation (Linux)

```bash
# Install Java 25
sudo apt-get install default-jdk

# Install C++ compiler
sudo apt-get install build-essential

# Install CMake (if needed)
sudo apt-get install cmake
```

### Build

```bash
# Full build with both systems
./gradlew clean build

# Build without tests
./gradlew build -x test

# Build with verbose output
./gradlew build --info
```

### Run

```bash
# Execute application
./gradlew run

# Expected output:
# Hello World!                          (Java method)
# [Gradle] Hello from Gradle C++ Plugin!
# Hello from Gradle C++ Plugin!         (Native method 1)
# [CMake] Hello from CMake Build System!
# Hello from CMake Build System!        (Native method 2)
```

### Test

```bash
# Run all tests
./gradlew test

# Expected output:
# ✓ appHasAGreeting()              (Java method)
# ✓ appHasGradleNativeGreeting()   (Gradle native method)
# ✓ appHasCMakeNativeGreeting()    (CMake native method)
```

---

## 🧬 Java Code Structure

### App.java - Main Application

**Static Initializer:**
```java
static {
    // Loads native library when class is initialized
    System.loadLibrary("greetings");
}
```
- Executed when `App` class is first loaded
- Loads `libgreetings.so` into JVM memory
- Must run before any native methods are called

**Native Methods:**
```java
// Gradle C++ Plugin implementation
public native String greetingFromGradle();

// CMake implementation
public native String greetingFromCMake();
```

**Java Method (for comparison):**
```java
public static String getGreeting() {
    return "Hello World!";
}
```

**Main Method:**
```java
public static void main(String[] args) {
    App app = new App();
    System.out.println(app.getGreeting());         // Java
    System.out.println(app.greetingFromGradle());  // Native 1
    System.out.println(app.greetingFromCMake());   // Native 2
}
```

---

## 🔧 C++ Code Structure

### greetings.cpp (Gradle Version)

**JNI Function Name:** `Java_org_example_App_greetingFromGradle`

**Function Breakdown:**
```
Java_org_example_App_greetingFromGradle
│    │   │      │
└────┼───┼──────┴─ JNI naming convention
     │   │
     │   └──────── Class name: App
     └──────────── Package: org.example
```

**Implementation:**
```cpp
extern "C" {
    JNIEXPORT jstring JNICALL Java_org_example_App_greetingFromGradle(
        JNIEnv *env,      // JNI environment interface
        jobject obj       // This object reference
    ) {
        // Create C++ string
        std::string greeting = "Hello from Gradle C++ Plugin!";
        
        // Print to stdout
        std::cout << "[Gradle] " << greeting << std::endl;
        
        // Convert to Java string (jstring) and return
        return env->NewStringUTF(greeting.c_str());
    }
}
```

**Key Elements:**
- `extern "C"`: C linkage (no C++ name mangling)
- `JNIEXPORT`: Mark as exported from DLL/SO
- `jstring`: JNI string type (Java String reference)
- `JNIEnv *env`: Provides access to JNI functions like NewStringUTF()
- `greeting.c_str()`: Convert std::string to const char*

### greetings_cmake.cpp (CMake Version)

**Identical structure to greetings.cpp:**
- Function name: `Java_org_example_App_greetingFromCMake`
- Same implementation pattern
- Different message: `"Hello from CMake Build System!"`
- Different console prefix: `[CMake]`

---

## 📊 Build Configuration

### app/build.gradle (PRIMARY)

**Plugins:**
```gradle
plugins {
    id 'application'  // Java application
    id 'cpp'          // C++ compilation
}
```

**CMake Task:**
```gradle
task buildNativeWithCMake(type: Exec) {
    // Executes: cmake && cmake --build .
    // Output: build_cmake/lib/libgreetings.so
}
build.dependsOn buildNativeWithCMake  // CMake runs BEFORE Gradle
```

**Gradle C++ Configuration:**
```gradle
model {
    components {
        greetings(NativeLibrarySpec) {
            sources {
                cpp {
                    source {
                        srcDir "src/main/cpp"
                        // BOTH files compile here
                        include "**/greetings.cpp", "**/greetings_cmake.cpp"
                    }
                }
            }
        }
    }
}
```

**Library Path:**
```gradle
// Both libraries must be in path for JNI
systemProperty 'java.library.path', 
    "${buildDir}/libs/greetings/shared:${rootProject.projectDir}/build_cmake/lib"
```

### CMakeLists.txt

**Create Library:**
```cmake
add_library(greetings SHARED
    app/src/main/cpp/greetings.cpp
    app/src/main/cpp/greetings_cmake.cpp
)
```

**Compiler Flags:**
```cmake
target_compile_options(greetings PRIVATE -Wall -Wextra -O2)
```
- `-Wall`: All warnings
- `-Wextra`: Extra warnings
- `-O2`: Medium optimization

**JNI Linking:**
```cmake
target_link_libraries(greetings PRIVATE ${JNI_LIBRARIES})
```

---

## 🧪 Testing

### AppTest.java

**Test 1: Java Method**
```java
@Test
void appHasAGreeting() {
    App app = new App();
    assertNotNull(app.getGreeting());
}
```
Verifies pure Java method works.

**Test 2: Gradle Native Method**
```java
@Test
void appHasGradleNativeGreeting() {
    App app = new App();
    String greeting = app.greetingFromGradle();
    assertNotNull(greeting);
    assertTrue(greeting.contains("Gradle"));
}
```
Verifies Gradle C++ compilation and JNI binding.

**Test 3: CMake Native Method**
```java
@Test
void appHasCMakeNativeGreeting() {
    App app = new App();
    String greeting = app.greetingFromCMake();
    assertNotNull(greeting);
    assertTrue(greeting.contains("CMake"));
}
```
Verifies CMake compilation and JNI binding.

---

## 📚 JNI Concepts

### Native Method Naming

All JNI functions must follow this pattern:
```
Java_<package>_<class>_<method>
```

**Example:** For `App.java` in package `org.example` with method `greetingFromGradle()`:
- Package: `org.example` → `org_example` (dots become underscores)
- Class: `App` → `App`
- Method: `greetingFromGradle` → `greetingFromGradle`
- **Result:** `Java_org_example_App_greetingFromGradle`

### Type Mappings

| Java Type | JNI Type | C/C++ Type |
|-----------|----------|-----------|
| String | jstring | jobject (with special handling) |
| int | jint | int |
| void | void | void |
| Object | jobject | pointer |

### JNIEnv Functions

Common functions in `JNIEnv *env`:
- `env->NewStringUTF(const char*)` - Create Java String from C string
- `env->GetStringUTFChars(jstring, jboolean*)` - Extract C string from Java String
- `env->ReleaseStringUTFChars(jstring, const char*)` - Release C string

---

## 🎯 Output Explanation

```bash
$ ./gradlew run

Hello World!                          ← Java method (getGreeting)
[Gradle] Hello from Gradle C++ Plugin! ← Console output from greetingFromGradle()
Hello from Gradle C++ Plugin!         ← Return value from greetingFromGradle()
[CMake] Hello from CMake Build System! ← Console output from greetingFromCMake()
Hello from CMake Build System!        ← Return value from greetingFromCMake()
```

**Flow:**
1. JVM executes `main()`
2. `new App()` triggers static initializer → loads libgreetings.so
3. `getGreeting()` calls Java implementation
4. `greetingFromGradle()` calls native function from Gradle compilation
5. `greetingFromCMake()` calls native function from CMake compilation
6. Both return jstring values converted to Java String by JNI

---

## 🔀 Alternative: CMake-Only Build

If you want to use CMake instead of Gradle plugin:

```bash
# Use CMake configuration
cp app/build.gradle app/build.gradle.plugin
cp build.gradle.cmake app/build.gradle

# Build with CMake
./gradlew clean build

# Switch back to Gradle
cp app/build.gradle.plugin app/build.gradle
./gradlew clean build
```

---

## 🛠️ Common Tasks

| Task | Command |
|------|---------|
| Clean build | `./gradlew clean build` |
| Build without tests | `./gradlew build -x test` |
| Run application | `./gradlew run` |
| Run tests | `./gradlew test` |
| See test report | Open `app/build/reports/tests/test/index.html` |
| Verbose build | `./gradlew build --info` |
| Check libraries | `ls app/build/libs/greetings/shared/` |
| Check CMake output | `ls build_cmake/lib/` |

---

## 🐛 Troubleshooting

### "Cannot find libgreetings.so"
```bash
# Verify library exists
ls app/build/libs/greetings/shared/libgreetings.so
ls build_cmake/lib/libgreetings.so

# Rebuild if missing
./gradlew clean build
```

### "UnsatisfiedLinkError" for native methods
```bash
# java.library.path must include both directories
# Verify in app/build.gradle:
systemProperty 'java.library.path', 
    "${buildDir}/libs/greetings/shared:${rootProject.projectDir}/build_cmake/lib"
```

### "CMake not found"
```bash
# Install CMake
sudo apt-get install cmake

# Verify installation
cmake --version
```

### Compilation errors in C++
```bash
# Check exact error
cat app/build/tmp/compileGreetingsSharedLibraryGreetingsCpp/output.txt

# Common issues:
# - Missing JNI headers: Verify JAVA_HOME points to JDK
# - Function name mismatch: Check JNI naming convention
# - Missing includes: Add #include <jni.h>
```

---

## 📖 Learning Resources

### JNI Documentation
- [Official JNI Specification](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/)
- [JNI Programmer's Guide](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/design.html)

### Build Systems
- [Gradle C++ Plugin](https://docs.gradle.org/current/userguide/cpp_plugin.html)
- [CMake Tutorial](https://cmake.org/cmake/help/latest/guide/tutorial/)

### Java/C++ Integration
- [Java Native Methods](https://docs.oracle.com/en/java/javase/tutorial/jni/)
- [Type Mappings in JNI](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/types.html)

---

## 📝 File Descriptions

| File | Purpose |
|------|---------|
| `app/build.gradle` | PRIMARY build config - activates both Gradle and CMake |
| `app/build.gradle.plugin` | BACKUP - Gradle plugin only (for switching) |
| `build.gradle.cmake` | ALTERNATIVE - CMake only (for switching) |
| `CMakeLists.txt` | CMake build configuration |
| `app/src/main/java/org/example/App.java` | Main Java application with 2 native methods |
| `app/src/main/cpp/greetings.cpp` | Gradle C++ implementation |
| `app/src/main/cpp/greetings_cmake.cpp` | CMake C++ implementation |
| `app/src/test/java/org/example/AppTest.java` | JUnit 5 tests for both methods |

---

## 🎓 Learning Objectives

After completing this project, you will understand:

1. **JNI Concepts**
   - Native method declaration and implementation
   - Type mappings between Java and C++
   - JNIEnv interface and common functions

2. **Build Systems**
   - Gradle C++ Plugin integration
   - CMake configuration and linking
   - Dual build system activation

3. **Java-C++ Integration**
   - Library loading with System.loadLibrary()
   - Native method invocation
   - String conversion between Java and C++

4. **Compiler Configuration**
   - Include paths for JNI headers
   - Linker flags for JVM library
   - Compiler optimization and warnings

5. **Testing & Debugging**
   - JUnit testing of native methods
   - Error diagnosis (UnsatisfiedLinkError, compilation errors)
   - Library path configuration

---

## 👨‍💻 Author

**ArkadiuszKubiak** - JNI Course Educational Project

---

## 📄 Technology Stack

- **Java:** 25 (JDK 25, Runtime 17.0.16)
- **C++:** C++11 or later
- **Build Systems:** Gradle 9.1.0, CMake 3.10+
- **Compiler:** GCC 11.4.0 (Linux)
- **Testing:** JUnit 5 (Jupiter)
- **Platform:** Linux (primary), macOS (supported)

---

## 🔗 Quick Links

```bash
# Clone repository
git clone https://github.com/ArkadiuszKubiak/JNI_TUTORIAL.git
cd lib-from-scratch

# Build & Run
./gradlew clean build
./gradlew run

# Test
./gradlew test
```

---

**Last Updated:** November 11, 2025
**Project Status:** ✓ Complete and Tested
